package ru.insoft.archive.vkks.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.validation.Validation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import static ru.insoft.archive.vkks.converter.Config.dbPrefix;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.error.ErrorCreateXlsFile;
import ru.insoft.archive.vkks.converter.error.WrongModeException;
import javafx.application.Platform;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Font;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;
import ru.insoft.archive.vkks.converter.service.Service;

/**
 * Обрабатывает xls файлы, сопоставляя их с данными в базе. Также копирует pdf
 * файлы из папки с базой в папку с xls файлами.
 *
 * @author Благодатских С.
 */
public class Worker extends Thread {

	private enum ValueType {

		STRING, INTEGER, CALENDAR
	}

	private boolean commit = false;

	/**
	 * Сигнализирует о завершении работы
	 */
	private final BooleanProperty done = new SimpleBooleanProperty(false);

	private final ConvertMode mode;
	private final String xlsDir;
	private final Path xlsPathDir;
	private final EntityManager em;
	private final TextArea logPanel;
	private final Stat stat = new Stat();
	private static final javax.validation.Validator validator
			= Validation.buildDefaultValidatorFactory().getValidator();

	private final Service xlsService;

	public Worker(String accessDb, TextArea logPanel, ConvertMode mode) throws WrongModeException {
		this.xlsPathDir = Paths.get(accessDb).getParent();
		this.xlsDir = xlsPathDir.toString();
		this.logPanel = logPanel;
		this.mode = mode;

		Properties props = new Properties();
		props.put("javax.persistence.jdbc.url", dbPrefix + accessDb);
		em = Persistence.createEntityManagerFactory("PU", props).createEntityManager();
		xlsService = Service.getInstance(mode, xlsPathDir).orElseThrow(() -> {
			return new WrongModeException("Не опеределен формирователь данных для режима: " + mode);
		});
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
			convertFewDelo(xlsService.getDelos(em));
		} finally {
			updateInfo(stat.toString());
			done.set(true);
		}
	}

	/**
	 * Конвертирует данные из mdb для нескольких записей в таблице Delo в
	 * файловое дерево
	 *
	 * @param dela записи из mdb
	 * @param fileName имя файла для записи данных
	 * @return в случае сигнала прервать операцию возвращаем false
	 */
	private void convertFewDelo(List<Delo> dela) {

		Workbook wb = new HSSFWorkbook();
		Sheet deloSheet = wb.createSheet("Дело");
		boolean createHeaders = true;
		int rowNumber = 1;
		for (Delo d : tuneDelas(dela)) {
			if (commit) {
				return;
			}
			++stat.cases;
			try {
				createDelo(d, deloSheet, wb, rowNumber++, createHeaders);
			} catch (WrongPdfFile ex) {
				updateInfo("Дело " + d.getId() + " имеет ошибки: " + ex.getMessage());
				continue;
			}
			createHeaders = false;
			updateInfo("Создано дело с идентификатором " + d.getId());
			++stat.casesCreated;
		}

		if (!dela.isEmpty()) {
			try {
				writeData(wb, Paths.get(xlsDir, mode + ".xls"));
			} catch (ErrorCreateXlsFile ex) {
				updateInfo(ex.getMessage());
			}
		} else {
			updateInfo("дела не найдены");
		}
	}

	/**
	 * Немножко поколдуем
	 *
	 * @param dela
	 * @return
	 */
	private Collection<Delo> tuneDelas(List<Delo> dela) {
		Map<Key, Delo> map = new HashMap<>();
		for (Delo d : dela) {
			List<Document> docs = d.getDocuments();
			for (Document doc : docs) {
				doc.setReportFormNumber(d.getTom().toString());
			}
			Key key = new Key(d.getTitle(), docs.get(0).getDate().get(Calendar.YEAR));
			Delo newdelo = map.get(key);	
			if (newdelo == null) {
				map.put(key, d);
			} else {
				newdelo.getDocuments().addAll(docs);
			}
		}
		return map.values();
	}

	/**
	 * Создает запись о деле в листе Дело, и записи документов дела в листе
	 * Документы
	 */
	private void createDelo(Delo d, Sheet delaSheet, Workbook wb, int deloRowNumber,
			boolean createHeaders) throws WrongPdfFile {
		if (createHeaders) {
			setHeaders(Config.deloHeaders, wb, delaSheet);
		}

		fillDeloSheet(delaSheet, xlsService.getDelo(d), deloRowNumber);
		Sheet sheet = wb.createSheet("Документы" + deloRowNumber);

		fillDocsSheet(wb, sheet, 1, d);
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
	 * Добавляет запись на лист "Дело"
	 */
	private void fillDeloSheet(Sheet sheet, XLSDelo delo, int rowNumber) {
		Row row = sheet.createRow(rowNumber);
		setCellValue(row.createCell(0), delo.getIndex(), ValueType.STRING);
		setCellValue(row.createCell(1), delo.getTitle(), ValueType.STRING);
		setCellValue(row.createCell(2), delo.getTomNumber(), ValueType.INTEGER);
		row.createCell(3);
		setCellValue(row.createCell(4), delo.getStartDate(), ValueType.CALENDAR);
		setCellValue(row.createCell(5), delo.getEndDate(), ValueType.CALENDAR);
		row.createCell(6);
	}

	/**
	 * Заполняет страницу документов
	 */
	private void fillDocsSheet(Workbook wb, Sheet sheet, int rowNumber, Delo delo)
			throws WrongPdfFile {
		setHeaders(Config.docHeaders, wb, sheet);
		for (XLSDocument doc : xlsService.getDocuments(delo)) {
			createDocRecord(sheet.createRow(rowNumber++), doc);
			++stat.docs;
		}
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
			}
		}
	}

	/**
	 * Создает запись для документа
	 */
	private void createDocRecord(Row row, XLSDocument doc) {
		setCellValue(row.createCell(0), doc.getRegNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), doc.getRegDate(), ValueType.CALENDAR);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4).setCellValue(doc.getShortContent());
		row.createCell(5);
		row.createCell(6);
		setCellValue(row.createCell(7), doc.getPages(), ValueType.INTEGER);
		setCellValue(row.createCell(8), doc.getRemark(), ValueType.STRING);
		setCellValue(row.createCell(9), doc.getFiles(), ValueType.STRING);
		setCellValue(row.createCell(10), doc.getVidName(), ValueType.STRING);
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

}

class Key {

	public final String title;
	public final int year;

	public Key(String title, int year) {
		this.title = title;
		this.year = year;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof Key) {
			Key other = (Key) obj;
			return other.title.equals(title) && other.year == year;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 97 * hash + Objects.hashCode(this.title);
		hash = 97 * hash + this.year;
		return hash;
	}

}
