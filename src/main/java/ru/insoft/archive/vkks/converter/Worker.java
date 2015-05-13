package ru.insoft.archive.vkks.converter;

import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javafx.application.Platform;
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
import ru.insoft.archive.vkks.converter.error.WrongFormat;

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

	private final Set<String> createdFileNames = new HashSet<>();

	private final String xlsDir;
	private final EntityManager em;
	private final Path xlsDirPath;
	private final TextArea logPanel;
	private Connection connection;
	private final String accessDbDir;
	private String currentDirForFile;
	private final Stat stat = new Stat();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

	public Worker(String xlsDir, String accessDb, TextArea logPanel) {
		this.xlsDir = xlsDir;

		Properties props = new Properties();
		props.put("javax.persistence.jdbc.url", dbPrefix + accessDb);
		em = Persistence.createEntityManagerFactory("PU", props).createEntityManager();
		this.logPanel = logPanel;
		accessDbDir = Paths.get(accessDb).getParent().toString();
		xlsDirPath = Paths.get(xlsDir);
	}

	@Override
	public void run() {
		((List<Delo>) em.createQuery("SELECT d FROM Delo d", Delo.class).getResultList()).forEach(d -> {
			++stat.cases;
			String caseNumber = null;
			try {
				caseNumber = d.getCaseNumber();
				if (caseNumber == null || caseNumber.trim().isEmpty()) {
					throw new WrongFormat("Отсутствует номер дела");
				}
				Calendar start = d.getDateStart();
				Calendar end = d.getDateEnd();
				if (start == null || end == null) {
					++stat.casesSkip;
					return;
				}
				String fileName = caseNumber + "_"
						+ sdf.format(start.getTime()) + "-"
						+ sdf.format(end.getTime());

				while (createdFileNames.contains(fileName)) {
					fileName += "_1";
				}

				createdFileNames.add(fileName);

				Path fileXls = Paths.get(xlsDir, fileName + ".xls");
				Path pdfDir = Paths.get(xlsDir, fileName);
				Files.createDirectories(pdfDir);
				createDelo(d, fileXls, pdfDir);
				++stat.casesCreated;
			} catch (IOException ex) {
				updateInfo("Не могу создать директорию для дела " + caseNumber + ": " + ex.getMessage());
			} catch (WrongFormat ex) {
				updateInfo(ex.getMessage());
			}
		});
		updateInfo(stat.toString());
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
		fillDeloSheet(wb, wb.createSheet("Дело"), d);
		fillDocsSheet(wb, wb.createSheet("Документы"), d.getDocuments(), pdfDir);

		try (OutputStream ous = Files.newOutputStream(file)) {
			wb.write(ous);
		} catch (IOException ex) {
			updateInfo("Не могу создать файл " + file + ": " + ex.getMessage());
		}
	}

	/**
	 * Заполняет страницу дел
	 */
	private void fillDeloSheet(Workbook wb, Sheet sheet, Delo delo) {
		setHeaders(Config.deloHeaders, wb, sheet);

		Row row = sheet.createRow(1);
		setCellValue(row.createCell(0), delo.getCaseNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), delo.getDeloTitle(), ValueType.STRING);
		setCellValue(row.createCell(2), delo.getNumberTom(), ValueType.INTEGER);
		setCellValue(row.createCell(3), delo.getNumberPart(), ValueType.INTEGER);
		setCellValue(row.createCell(4), delo.getDateStart(), ValueType.CALENDAR);
		setCellValue(row.createCell(5), delo.getDateEnd(), ValueType.CALENDAR);
		setCellValue(row.createCell(6), delo.getRemarkDelo(), ValueType.STRING);
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
		for (int i = 0; i < size; ++i) {
			createDocRecord(sheet.createRow(i + 1), documents.get(i), dateStyle, pdfDir);
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
		if (doc.getPageS() == null || doc.getPageS().trim().isEmpty()) {
			++stat.docsSkip;
			return;
		}
		++stat.docs;
		setCellValue(row.createCell(0), doc.getDocNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), doc.getDateDoc(), ValueType.CALENDAR1, style);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4).setCellValue(doc.getDocTitle());
		row.createCell(5);
		row.createCell(6);
		setCellValue(row.createCell(8), doc.getRemarkDocument(), ValueType.STRING);
		String graph = doc.getGraph();
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
				+ "Создано xls файлов - %d\nПропущено дел - %d\nЗаписано документов - %d\n"
				+ "Пропущено документов - %d\n", cases, casesCreated, casesSkip, docs, docsSkip);
	}

}
