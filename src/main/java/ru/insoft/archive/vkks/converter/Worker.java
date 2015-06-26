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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.validation.Validation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import static ru.insoft.archive.vkks.converter.Config.dbPrefix;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.error.ErrorCreateXlsFile;
import ru.insoft.archive.vkks.converter.error.WrongModeException;
import org.apache.commons.io.FilenameUtils;
import com.itextpdf.text.pdf.PdfReader;
import javafx.application.Platform;
import javax.validation.ConstraintViolation;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

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
	private final Path xlsPathDir;
	private final EntityManager em;
	private final TextArea logPanel;
	private final Stat stat = new Stat();
	private static final javax.validation.Validator validator
			= Validation.buildDefaultValidatorFactory().getValidator();

	public Worker(String accessDb, TextArea logPanel, String mode) {
		this.xlsPathDir = Paths.get(accessDb).getParent();
		this.xlsDir = xlsPathDir.toString();
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
		} catch (WrongModeException ex) {
			updateInfo(ex.getMessage());
		} finally {
			updateInfo(stat.toString());
			done.set(true);
		}
	}

	/**
	 * Создает файл со сборниками
	 */
	private void createYearSbornik() throws WrongModeException {
		List<Delo> dela = em.createQuery("SELECT d FROM Delo d WHERE d.barCode in :codes", Delo.class)
				.setParameter("codes", Config.modeBarCodes.get(mode))
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
	private boolean convertFewDelo(List<Delo> dela) throws WrongModeException {

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
				try {
					createDelo(d, deloSheet, wb, rowNumber++, createHeaders);
				} catch (WrongPdfFile ex) {
					updateInfo(ex.getMessage());
					continue;
				}
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
	private void createDelo(Delo d, Sheet dela, Workbook wb, int deloRowNumber, boolean createHeaders) throws WrongModeException, WrongPdfFile {

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
	private void fillDeloSheet(Workbook wb, Sheet sheet, Delo delo, int rowNumber) throws WrongModeException {
		/*
		 Индекс дела = «1-4" + «значение года из \Delo\Date_end»
		 Заголовок дела = \Delo\Delo_title
		 № тома = \Delo\Number_tom, если \Delo\Number_tom = is Null, то \Delo\Number_tom = 1
		 Дата дела с = \Delo\Date_start
		 Дата дела по = \Delo\Date_end

		 Индекс дела = «1-1-5» + «-значение года из \Delo\Date_end»
		 Заголовок дела = \Delo\Delo_title
		 № тома = \Delo\Number_tom, если \Delo\Number_tom = is Null, то \Delo\Number_tom = 1
		 Дата дела с = \Delo\Date_start
		 Дата дела по = \Delo\Date_end

		 Индекс дела = «1-1-1» + «-1998»
		 Заголовок дела = \Delo\Delo_title
		 № тома = \Delo\Number_tom, если \Delo\Number_tom = is Null, то \Delo\Number_tom = 1
		 Дата дела с = 01.01.1998
		 Дата дела по = 31.12.1998
		 */

		String caseIndex;
		Calendar startDate;
		Calendar endDate;

		switch (mode) {
			case Config.MODE_1:
				endDate = delo.getEndDate();
				startDate = delo.getStartDate();
				caseIndex = "1-4" + endDate.get(Calendar.YEAR);
				break;
			case Config.MODE_2:
				endDate = delo.getEndDate();
				startDate = delo.getStartDate();
				caseIndex = "1-1-5-" + endDate.get(Calendar.YEAR);
				break;
			case Config.MODE_3:
				startDate = Calendar.getInstance();
				startDate.set(1998, 0, 1);
				endDate = Calendar.getInstance();
				endDate.set(1998, 11, 31);
				caseIndex = "1-1-1-1998";
				break;
			default:
				throw new WrongModeException("Неправильный режим работы: " + mode);
		}

		Row row = sheet.createRow(rowNumber);
		setCellValue(row.createCell(0), caseIndex, ValueType.STRING);
		setCellValue(row.createCell(1), delo.getTitle(), ValueType.STRING);
		setCellValue(row.createCell(2), delo.getTom(), ValueType.INTEGER);
		row.createCell(3);
		setCellValue(row.createCell(4), startDate, ValueType.CALENDAR);
		setCellValue(row.createCell(5), endDate, ValueType.CALENDAR);
		row.createCell(6);
	}

	/**
	 * Заполняет страницу документов
	 */
	private int fillDocsSheet(Workbook wb, Sheet sheet, int rowNumber, Delo delo,
			CellStyle dateStyle, boolean createHeaders) throws WrongPdfFile, WrongModeException {
		if (createHeaders) {
			setHeaders(Config.docHeaders, wb, sheet);
		}

		String deloGraph = delo.getGraph();
		if (deloGraph != null && !deloGraph.trim().isEmpty()) {
			/*
			 № регистрации = "б/н"
			 Дата регистрации = \Delo\Date_end
			 Краткое содержание = \Delo\Delo_title
			 Количество листов = считать количество страниц в прикрепленном PDF-файле
			 Файлы = \Delo\Graph_delo
			 Примечание = is Null
			 Наименование вида = "Титульный лист"

			 № регистрации = "б/н"
			 Дата регистрации = \Delo\Date_end
			 Краткое содержание = \Delo\Delo_title
			 Количество листов = считать количество страниц в прикрепленном PDF-файле
			 Файлы = \Delo\Graph_delo
			 Примечание = is Null
			 Наименование вида = "Титульный лист"

			 № регистрации = "б/н"
			 Дата регистрации = 31.12.1998
			 Краткое содержание = \Delo\Delo_title
			 Количество листов = считать количество страниц в прикрепленном PDF-файле
			 Файлы = \Delo\Graph_delo
			 Примечание = is Null
			 Наименование вида = "Титульный лист"
			 */
			Calendar date;
			if (mode.equals(Config.MODE_3)) {
				date = Calendar.getInstance();
				date.set(1998, 11, 31);
			} else {
				date = delo.getEndDate();
			}
			String pdfFileName = getPdfLink(delo.getGraph());
			createDocRecord(sheet.createRow(rowNumber++), new Document("б/н",
					date, delo.getTitle(), countPages(pdfFileName), pdfFileName,
					"", "Титульный лист"));
		}

		List<Document> documents = delo.getDocuments();
		int size = documents.size();
		for (int i = 0; i < size; ++i) {
			Document doc = documents.get(i);
			/*
			 № регистрации = \Document\Report_form_number, если \Document\Report_form_number = is Null, то \Document\Report_form_number = "б/н"
			 Дата регистрации = \Document\Date_doc, если \Document\Date_doc= is Null, то \Document\Date_doc = \Delo\Date_end
			 Краткое содержание = \Document\Doc_title
			 Количество листов = считать количество страниц в прикрепленном PDF-файле
			 Файлы = \Document\Graph
			 Примечание = "\Document\Law_court_name"+" 
			 \Document\Subject_name_RF" " \Document\Report_period"
			 " \Document\Report_type", если какой-либо из индексов пустой, значит ничего и не кладем
			 Наименование вида = "Регламентная судебная статистика"

			 № регистрации = «МЮ-б\н»"
			 Дата регистрации = \Delo\Date_end
			 Краткое содержание = \Document\Doc_title
			 Количество листов = считать количество страниц в прикрепленном PDF-файле
			 Файлы = \Document\Graph
			 Примечание = "\Document\Law_court_name"+
			 " \Document\Subject_name_RF" " \Document\Report_period"
			 " \Document\Report_type", если какой-либо из индексов пустой, значит ничего и не кладем
			 Наименование вида = "судебная статистика"

			 № регистрации = «МЮ-б\н»"
			 Дата регистрации = 31.12.1998
			 Краткое содержание = \Document\Doc_title
			 Количество листов = считать количество страниц в прикрепленном PDF-файле
			 Файлы = \Document\Graph
			 Наименование вида = "обзоры_доклады" 
			 */
			String regNumber;
			Calendar date;
			String remark;
			String type; 
			if (mode.equals(Config.MODE_1)) {
			} else {
			}
			switch (mode) {
				case Config.MODE_1:
					regNumber = doc.getReportFormNumber();
					date = doc.getDate();
					if (date == null) {
						date = delo.getEndDate();
					}
					type = "Регламентная судебная статистика";
					remark = doc.getLawCourtName() + doc.getSubjectNameRF() + doc.getReportPeriod() + doc.getReportType();
					break;
				case Config.MODE_2:
					regNumber = "МЮ-б\\н";
					date = delo.getEndDate();
					type = "судебная статистика";
					remark = doc.getLawCourtName() + doc.getSubjectNameRF() + doc.getReportPeriod() + doc.getReportType();
					break;
				case Config.MODE_3:
					regNumber = "МЮ-б\\н";
					date = Calendar.getInstance();
					date.set(1998, 11, 31);
					type = "обзоры_доклады";
					remark = "";
					break;
				default:
					throw new WrongModeException("Неправильный режим работы: " + mode);
			}
			String docGraph = getPdfLink(doc.getGraph());

			createDocRecord(sheet.createRow(rowNumber++), new Document(regNumber,
					date, doc.getTitle(), countPages(docGraph), docGraph, remark, type));
			++stat.docs;
		}
		return rowNumber;
	}

	/**
	 * Подситываем кол-во страниц документов на входе получает относительный
	 * путь к файлу в формате Windows
	 */
	private String countPages(String pdfFileName) throws WrongPdfFile {
		try {
			pdfFileName = FilenameUtils.separatorsToSystem(pdfFileName);
			PdfReader reader = new PdfReader(xlsPathDir.resolve(pdfFileName).toString());
			Integer pages = reader.getNumberOfPages();
			reader.close();
			return pages.toString();
		} catch (IOException ex) {
			throw new WrongPdfFile("Невозможно получить кол-во страниц " + pdfFileName + ": " + ex.getMessage());
		}
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
	private void createDocRecord(Row row, Document doc) {
		setCellValue(row.createCell(0), doc.getReportFormNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), doc.getDate(), ValueType.CALENDAR);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4).setCellValue(doc.getTitle());
		row.createCell(5);
		row.createCell(6);

		setCellValue(row.createCell(7), doc.getPages(), ValueType.STRING);

		setCellValue(row.createCell(8), doc.getRemark(), ValueType.STRING);
		setCellValue(row.createCell(9), doc.getGraph(), ValueType.STRING);
		setCellValue(row.createCell(10), doc.getType(), ValueType.STRING);
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
		if (mode.equals(Config.MODE_3))
			return true;
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
