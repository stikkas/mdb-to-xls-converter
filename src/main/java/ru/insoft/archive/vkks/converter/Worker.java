package ru.insoft.archive.vkks.converter;

import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import org.apache.commons.io.FilenameUtils;
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

	private final boolean opis;
	private final String xlsDir;
	private final EntityManager em;
	private final Path xlsDirPath;
	private final TextArea logPanel;
	private final String accessDbDir;
	private final Stat stat = new Stat();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

	public Worker(String xlsDir, String accessDb, TextArea logPanel, boolean opis) {
		this.xlsDir = xlsDir;

		Properties props = new Properties();
		props.put("javax.persistence.jdbc.url", dbPrefix + accessDb);
		em = Persistence.createEntityManagerFactory("PU", props).createEntityManager();
		this.logPanel = logPanel;
		accessDbDir = Paths.get(accessDb).getParent().toString();
		xlsDirPath = Paths.get(xlsDir);
		this.opis = opis;
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
			sleep(10000);
		/*	
			if (opis) {
				createOpis();
			} else {
				((List<Delo>) em.createQuery("SELECT d FROM Delo d", Delo.class).getResultList()).forEach(d -> {
					if (!commit) {
						++stat.cases;
						String caseNumber = null;
						try {
							if (checkCaseNumber(d)) {
								caseNumber = d.getCaseNumber();

								String fileName = getDirNameForDocuments(d);
								Path fileXls = Paths.get(xlsDir, fileName + ".xls");
								Path pdfDir = Paths.get(xlsDir, fileName);
								Files.createDirectories(pdfDir);
								createDelo(d, fileXls, pdfDir);

								updateInfo("Создано дело с номером " + caseNumber);
								++stat.casesCreated;
							}
						} catch (IOException ex) {
							updateInfo("Не могу создать директорию для дела " + caseNumber + ": " + ex.getMessage());
						}
					}
				});
			}
			*/
		} catch (InterruptedException ex) {
			Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
		} finally {
			updateInfo(stat.toString());
			done.set(true);
		}
	}

	/**
	 * Создает файл с описью
	 */
	private void createOpis() {
		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Дело");
		setHeaders(Config.deloHeaders, wb, sheet);
		List<Delo> delas = em.createQuery("SELECT d FROM Delo d", Delo.class).getResultList();
		int size = delas.size();
		int row = 1;
		for (int i = 0; i < size; ++i) {
			if (commit) {
				break;
			}
			++stat.cases;
			Delo d = delas.get(i);
			if (checkCaseNumber(d)) {
				String caseNumber = d.getCaseNumber();
				String fileName = getDirNameForDocuments(d);
				Path pdfDir = Paths.get(xlsDir, fileName);
				try {
					Files.createDirectories(pdfDir);
					fillDeloSheet(wb, sheet, d, row);
					fillDocsSheet(wb, wb.createSheet("Документы" + row), d.getDocuments(), pdfDir);
					++row;
					updateInfo("Создано дело с номером " + caseNumber);
					++stat.casesCreated;
				} catch (IOException ex) {
					updateInfo("Не могу создать директорию для дела " + caseNumber + ": " + ex.getMessage());
				}
			}
		}
		try (OutputStream ous = Files.newOutputStream(Paths.get(xlsDir, "opis.xls"))) {
			wb.write(ous);
		} catch (IOException ex) {
			updateInfo("Не могу создать файл opis.xls: " + ex.getMessage());
		}

	}

	/**
	 * Проверяет правильность дела
	 *
	 * @param d дело
	 * @return в случае отсутствия номера или одной из дат, дело считается
	 * неправильным и пропускается
	 */
	private boolean checkCaseNumber(Delo d) {
		String caseNumber = d.getCaseNumber();
		if (caseNumber == null || caseNumber.trim().isEmpty()) {
			return false;
		}
		if (d.getStartDate() == null || d.getEndDate() == null) {
			++stat.casesSkip;
			return false;
		}

		return true;
	}

	/**
	 * Возвращает название папки куда будут складываться документы дела
	 *
	 * @param d дело
	 * @return
	 */
	private String getDirNameForDocuments(Delo d) {
		String fileName = d.getCaseNumber() + "_"
				+ sdf.format(d.getStartDate().getTime()) + "-"
				+ sdf.format(d.getEndDate().getTime());

		while (createdFileNames.contains(fileName)) {
			fileName += "_1";
		}

		createdFileNames.add(fileName);
		return fileName;
	}

	/**
	 * Создает файл с делом
	 *
	 * @param d дело
	 * @param file путь к xls файлу
	 * @param pdfDir директория для pdf файлов дела
	 */
	private void createDelo(Delo d, Path file, Path pdfDir) {

		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Дело");
		setHeaders(Config.deloHeaders, wb, sheet);
		fillDeloSheet(wb, sheet, d, 1);

		fillDocsSheet(wb, wb.createSheet("Документы"), d.getDocuments(), pdfDir);

		writeData(wb, file);
	}

	/**
	 * Записывает данные в файл xls
	 */
	private void writeData(Workbook wb, Path file) {
		try (OutputStream ous = Files.newOutputStream(file)) {
			wb.write(ous);
		} catch (IOException ex) {
			updateInfo("Не могу создать файл " + file + ": " + ex.getMessage());
		}
	}

	/**
	 * Заполняет страницу дел
	 */
	private void fillDeloSheet(Workbook wb, Sheet sheet, Delo delo, int rowNumber) {
		Row row = sheet.createRow(rowNumber);
		setCellValue(row.createCell(0), delo.getCaseNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), delo.getCaseTitle(), ValueType.STRING);
		setCellValue(row.createCell(2), delo.getTomNumber(), ValueType.INTEGER);
		setCellValue(row.createCell(3), delo.getNumberPart(), ValueType.INTEGER);
		setCellValue(row.createCell(4), delo.getStartDate(), ValueType.CALENDAR);
		setCellValue(row.createCell(5), delo.getEndDate(), ValueType.CALENDAR);
		setCellValue(row.createCell(6), delo.getCaseRemark(), ValueType.STRING);
	}

	/**
	 * Заполняет страницу документов
	 */
	private void fillDocsSheet(Workbook wb, Sheet sheet, List<Document> documents, Path pdfDir) {
		setHeaders(Config.docHeaders, wb, sheet);

		CellStyle dateStyle = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		dateStyle.setDataFormat(df.getFormat("m/d/yy"));

		int size = documents.size();
		int rowNumber = 1;
		for (int i = 0; i < size; ++i) {
			Document doc = documents.get(i);
			if (doc.getStartPage() == null) {
				++stat.docsSkip;
				continue;
			}
			createDocRecord(sheet.createRow(rowNumber++), doc, dateStyle, pdfDir);
			++stat.docs;
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
					cell.setCellValue(sdf.format(((Calendar) value).getTime()));
				case CALENDAR1:
					cell.setCellValue(sdf.format(((Calendar) value).getTime()));
			}
		}
	}

	/**
	 * Создает запись для документа
	 *
	 * @param row
	 * @param doc
	 */
	private void createDocRecord(Row row, Document doc, CellStyle style, Path pdfDir) {
		setCellValue(row.createCell(0), doc.getDocNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), doc.getDocDate(), ValueType.CALENDAR1, style);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4).setCellValue(doc.getDocTitle());
		row.createCell(5);
		row.createCell(6);
		setCellValue(row.createCell(8), doc.getDocRemark(), ValueType.STRING);
		String graph = doc.getPrikGraph();
		if (graph != null) {
			Path srcFile = getPathForLink(graph);
			int pages = getPagesOfPdf(srcFile.toString());
			if (pages != 0) {

				Path dstFile = pdfDir.resolve(srcFile.getFileName());
				if (!Files.exists(dstFile)) {
					try {
						Files.copy(srcFile, dstFile);
					} catch (IOException ex) {
						updateInfo("Ошибка копирования файла " + srcFile + " в "
								+ pdfDir + ": " + ex.getMessage());
					}
				} else {
					updateInfo("Файл " + dstFile + " уже существует");
				}
				int startIndex = xlsDirPath.getNameCount();
				int endIndex = dstFile.getNameCount();
				String relativeDstFileName = FilenameUtils.separatorsToWindows(
						dstFile.subpath(startIndex, endIndex).toString());
				setCellValue(row.createCell(9), relativeDstFileName, ValueType.STRING);
				row.createCell(7).setCellValue(pages);
			}
		} else {
			row.createCell(9);
		}
		setCellValue(row.createCell(10), doc.getDocType(), ValueType.STRING);
		row.createCell(11);
		row.createCell(12);
		row.createCell(13).setCellValue(doc.getStartPage());
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
	 * Преобразует ссылку из базы данных в абсолютный путь к исходному файлу. В
	 * базе данных ссылка представлена в Windows формате.
	 *
	 * @param link ссылка на файл данных
	 * @return пусть к файлу
	 */
	private Path getPathForLink(String link) {
		link = link.trim();
		if (link.startsWith("#")) {
			link = link.substring(1);
		}
		if (link.endsWith("#")) {
			link = link.substring(0, link.length() - 1);
		}
		return Paths.get(accessDbDir, FilenameUtils.separatorsToSystem(link));
	}

	/**
	 * Получает кол-во страниц в pdf документе
	 *
	 * @param filename имя файла
	 * @return количество страниц
	 */
	private int getPagesOfPdf(String filename) {
		try {
			return new PdfReader(filename).getNumberOfPages();
		} catch (IOException ex) {
			updateInfo("Невозможно получить кол-во страниц " + filename + ": " + ex.getMessage());
			return 0;
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

class Stat {

	/**
	 * Кол-во обработанных дел
	 */
	long cases;
	/**
	 * Кол-во созданных записей документов
	 */
	long docs;
	/**
	 * Кол-во пропущеных документов
	 */
	long docsSkip;
	/**
	 * Кол-во пропущеных дел
	 */
	long casesSkip;
	/**
	 * Кол-во созданных файлов дел
	 */
	long casesCreated;

	@Override
	public String toString() {
		return String.format("Обработано дел - %d\n"
				+ "Создано дел - %d\nПропущено дел - %d\nЗаписано документов - %d\n"
				+ "Пропущено документов - %d\n", cases, casesCreated, casesSkip, docs, docsSkip);
	}

}
