package ru.insoft.archive.vkks.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import static ru.insoft.archive.vkks.converter.Config.dbPrefix;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.error.ErrorCreateXlsFile;

/**
 * Обрабатывает xls файлы, сопоставляя их с данными в базе. Также копирует pdf
 * файлы из папки с базой в папку с xls файлами.
 *
 * @author Благодатских С.
 */
public class Worker extends Thread {

	private enum ValueType {

		STRING, INTEGER, CALENDAR, CALENDAR1
	}

	private boolean commit = false;

	private final Set<String> createdFileNames = new HashSet<>();

	/**
	 * Сигнализирует о завершении работы
	 */
	private final BooleanProperty done = new SimpleBooleanProperty(false);

	private final String mode;
	private final String xlsDir;
	private final EntityManager em;
	private final TextArea logPanel;
	private final Stat stat = new Stat();
	private static final javax.validation.Validator validator
			= Validation.buildDefaultValidatorFactory().getValidator();

	public Worker(String accessDb, TextArea logPanel, String mode) {
		this.xlsDir = Paths.get(accessDb).getParent().toString();
		this.logPanel = logPanel;
		this.mode = mode;

		Properties props = new Properties();
		props.put("javax.persistence.jdbc.url", dbPrefix + accessDb);
		em = Persistence.createEntityManagerFactory("PU", props).createEntityManager();
	}

	/**
	 * Устанавливает признак завершения работы
	 */
	public void cancel() {
		commit = true;
	}

	@Override
	public void run() {
		try {
			createYearSbornik();
		} finally {
			updateInfo(stat.toString());
			done.set(true);
		}
	}

	/**
	 * Создает файл со сборниками
	 */
	private void createYearSbornik() {
		List<Delo> dela = em.createQuery("SELECT d FROM Delo d WHERE d.title LIKE :title ORDER BY d.startDate ASC, d.tom ASC", Delo.class)
				.setParameter("title", Config.modeTitles.get(mode))
				.getResultList();

		convertFewDelo(dela);
	}

	/**
	 * Конвертирует данные из mdb для нескольких записей в таблице Delo в
	 * файловое дерево
	 *
	 * @param dela записи из mdb
	 * @param fileName имя файла для записи данных
	 * @return в случае сигнала прервать операцию возвращаем false
	 */
	private boolean convertFewDelo(List<Delo> dela) {

		Workbook wb = new HSSFWorkbook();
		Sheet deloSheet = wb.createSheet("Дело");
		boolean createHeaders = true;
		int rowNumber = 1;
		for (Delo d : dela) {
			if (commit) {
				return false;
			}
			++stat.cases;
			if (checkDelo(d)) {
				createDelo(d, deloSheet, wb, rowNumber++, createHeaders);
				createHeaders = false;
				updateInfo("Создано дело с идентификатором " + d.getId());
				++stat.casesCreated;
			}
		}

		if (!dela.isEmpty()) {
			Delo d = dela.get(0);
			try {
				writeData(wb, Paths.get(xlsDir, mode + ".xls"));
			} catch (ErrorCreateXlsFile ex) {
				updateInfo(ex.getMessage());
			}
		} else {
			updateInfo("дела не найдены");
		}
		return true;
	}

	/**
	 * Создает запись о деле в листе Дело, и записи документов дела в листе
	 * Документы
	 */
	private void createDelo(Delo d, Sheet dela, Workbook wb, int deloRowNumber, boolean createHeaders) {

		if (createHeaders) {
			setHeaders(Config.deloHeaders, wb, dela);
		}

		fillDeloSheet(wb, dela, d, deloRowNumber);

		CellStyle dateStyle = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		dateStyle.setDataFormat(df.getFormat("m/d/yy"));

		Sheet sheet = wb.createSheet("Документы" + deloRowNumber);

		int docRowNumber = 1;

		// Разбираем документы на просто документы и на листы-заверители и внутренние описи
		fillDocsSheet(wb, sheet, docRowNumber, d, dateStyle, true);

	}

	/**
	 * Записывает данные в файл xls
	 */
	private void writeData(Workbook wb, Path file) throws ErrorCreateXlsFile {
		try (OutputStream ous = Files.newOutputStream(file)) {
			wb.write(ous);
		} catch (IOException ex) {
			throw new ErrorCreateXlsFile("Не могу создать файл " + file + ": " + ex.getMessage());
		}
	}

	/**
	 * Заполняет страницу дел
	 */
	private void fillDeloSheet(Workbook wb, Sheet sheet, Delo delo, int rowNumber) {
		Row row = sheet.createRow(rowNumber);
		setCellValue(row.createCell(0), "1-1-6"
				+ delo.getStartDate().get(Calendar.YEAR), ValueType.STRING);
		setCellValue(row.createCell(1), delo.getTitle(), ValueType.STRING);
		setCellValue(row.createCell(2), delo.getTom(), ValueType.INTEGER);
		row.createCell(3);
		setCellValue(row.createCell(4), delo.getStartDate(), ValueType.CALENDAR);
		setCellValue(row.createCell(5), delo.getEndDate(), ValueType.CALENDAR);
		row.createCell(6);
	}

	/**
	 * Заполняет страницу документов
	 */
	private int fillDocsSheet(Workbook wb, Sheet sheet, int rowNumber, Delo delo,
			CellStyle dateStyle, boolean createHeaders) {
		if (createHeaders) {
			setHeaders(Config.docHeaders, wb, sheet);
		}

		List<Document> documents = delo.getDocuments();
		int size = documents.size();
		for (int i = 0; i < size; ++i) {
			Document doc = documents.get(i);
			createDocRecord(sheet.createRow(rowNumber++), doc, delo, dateStyle);
			++stat.docs;
		}
		return rowNumber;
	}

	/**
	 * Устанавливает значение для ячейки, если значение null - то ничего не
	 * делает.
	 */
	private void setCellValue(Cell cell, Object value, ValueType type, CellStyle style) {
		cell.setCellStyle(style);
		setCellValue(cell, value, type);
	}

	/**
	 * Устанавливает значение ячейки
	 */
	private void setCellValue(Cell cell, Object value, ValueType type) {
		if (value != null) {
			switch (type) {
				case STRING:
					cell.setCellValue((String) value);
					break;
				case INTEGER:
					cell.setCellValue((Integer) value);
					break;
				case CALENDAR:
					cell.setCellType(Cell.CELL_TYPE_NUMERIC);
					cell.setCellValue(Config.sdf.format(((Calendar) value).getTime()));
				case CALENDAR1:
					cell.setCellValue(Config.sdf.format(((Calendar) value).getTime()));
			}
		}
	}

	/**
	 * Создает запись для документа
	 */
	private void createDocRecord(Row row, Document doc, Delo delo, CellStyle style) {
		setCellValue(row.createCell(0), delo.getTom(), ValueType.INTEGER);
		setCellValue(row.createCell(1), String.format("01.%02d.%d", delo.getTom(),
				delo.getStartDate().get(Calendar.YEAR)), ValueType.STRING);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4).setCellValue("Ежемесячный информационный бюллетень");
		row.createCell(5);
		row.createCell(6);

		setCellValue(row.createCell(7), 1, ValueType.INTEGER);

		row.createCell(8);
		setCellValue(row.createCell(9), getPdfLink(delo.getGraph())
				+ ";" + getPdfLink(doc.getGraph()), ValueType.STRING);
		setCellValue(row.createCell(10), "Ежемесячный информационный бюллетень ВИНИТИ", ValueType.STRING);
		row.createCell(11);
		row.createCell(12);
		row.createCell(13);
	}

	/**
	 * Создает заголовки для листа
	 *
	 * @param headers
	 * @param wb
	 * @param sheet
	 */
	private void setHeaders(String[] headers, Workbook wb, Sheet sheet) {
		CellStyle style = wb.createCellStyle();
		Font font = wb.createFont();
		font.setFontHeightInPoints((short) 10);
		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
		style.setFont(font);
		style.setAlignment(CellStyle.ALIGN_CENTER);

		Row row = sheet.createRow(0);
		for (int i = 0; i < headers.length; ++i) {
			Cell cell = row.createCell(i);
			cell.setCellValue(headers[i]);
			cell.setCellStyle(style);
		}
	}

	/**
	 * Преобразует ссылку из базы данных в относительный путь к исходному файлу.
	 * В базе данных ссылка представлена в Windows формате. Удаляются лишние
	 * символы на начале и конце.
	 *
	 * @param link ссылка на файл данных
	 * @return путь к файлу в Windows формате
	 */
	private String getPdfLink(String link) {
		if (link != null) {
			link = link.trim();
			if (link.startsWith("#")) {
				link = link.substring(1);
			}
			if (link.endsWith("#")) {
				link = link.substring(0, link.length() - 1);
			}
		}
		return link;
	}

	/**
	 * Пишет информацию о ходе выполнения в статустую панель.
	 *
	 * @param message сообщение
	 */
	private void updateInfo(String message) {
		Platform.runLater(() -> logPanel.insertText(0, message + "\n"));
	}

	public BooleanProperty doneProperty() {
		return done;
	}

	/**
	 * Проверяет правильность оформления дела
	 *
	 * @param delo интересуемое дело
	 * @return в случае правильного оформления - true, иначе - false
	 */
	private boolean checkDelo(Delo delo) {
		Set<ConstraintViolation<Delo>> errors = validator.validate(delo);
		boolean valid = errors.isEmpty();
		StringBuilder builder = null;
		if (!valid) {
			builder = new StringBuilder(String
					.format("Дело с ID = %d имеет следующие ошибки:\n", delo.getId()));
			for (ConstraintViolation<Delo> error : errors) {
				builder.append("\t").append(error.getMessage());
			}
		} 

		if (builder != null) {
			updateInfo(builder.toString());
		}
		return valid;
	}
}
