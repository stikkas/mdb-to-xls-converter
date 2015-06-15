package ru.insoft.archive.vkks.converter;

import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.TextArea;
import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
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
import ru.insoft.archive.vkks.converter.error.ErrorCreateXlsFile;
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

	private final WorkMode mode;
	private final Integer caseId;
	private final String xlsDir;
	private final EntityManager em;
	private final Path xlsDirPath;
	private final TextArea logPanel;
	private final String accessDbDir;
	private final Stat stat = new Stat();
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	private static final javax.validation.Validator validator
			= Validation.buildDefaultValidatorFactory().getValidator();

	public Worker(String xlsDir, String accessDb, TextArea logPanel, WorkMode mode) {
		this(xlsDir, accessDb, logPanel, mode, null);
	}

	public Worker(String xlsDir, String accessDb, TextArea logPanel, WorkMode mode, Integer caseId) {
		this.xlsDir = xlsDir;
		this.logPanel = logPanel;
		this.mode = mode;
		this.caseId = caseId;

		Properties props = new Properties();
		props.put("javax.persistence.jdbc.url", dbPrefix + accessDb);
		em = Persistence.createEntityManagerFactory("PU", props).createEntityManager();
		accessDbDir = Paths.get(accessDb).getParent().toString();
		xlsDirPath = Paths.get(xlsDir);
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
			switch (mode) {
				case BY_ID:
					createForId();
					break;
				case CASE_XLS:
					createForAll();
					break;
				default: // GROUP_CASE_XLS
					createForAllGroups();
			}
		} finally {
			updateInfo(stat.toString());
			done.set(true);
		}
	}

	/**
	 * Выбирает из базы дело с заданным ID и формирует для него xls и копирует
	 * соответсвующие pdf
	 */
	private void createForId() {
		Delo delo = em.find(Delo.class, caseId);
		if (delo == null) {
			updateInfo("Дело не найдено");
			return;
		}
		++stat.cases;
		convertOneDelo(delo);
	}

	/**
	 * Формирует для каждой записи в таблице Case отдельный xls и копирует
	 * соответсвующие pdf
	 */
	private void createForAll() {
		((List<Delo>) em.createQuery("SELECT d FROM Delo d", Delo.class).getResultList()).forEach(d -> {
			if (!commit) {
				++stat.cases;
				convertOneDelo(d);
			}
		});
	}

	/**
	 * Формирует для каждой группы томов одного дела за один год отдельный xls и
	 * копирует соответствующие pdf. Год берется из поля end_date.
	 */
	private void createForAllGroups() {
		for (Entry<GroupDeloKey, List<Delo>> e : ((List<Delo>) em.createQuery("SELECT d FROM Delo d", Delo.class)
				.getResultList()).stream()
				.collect(Collectors.groupingBy(delo
								-> new GroupDeloKey(delo.getEndDate().get(Calendar.YEAR), delo.getCaseNumber())))
				.entrySet()) {

			try {
				if (!createOpis(e.getKey(), e.getValue())) {
					break;
				}
			} catch (WrongPdfFile | IOException wex) {
				updateInfo(wex.getMessage());
			}
		}

	}

	/**
	 * Конвертирует данные из mdb для одной записи в таблице Case в файловое
	 * дерево
	 *
	 * @param d запись из mdb
	 */
	private void convertOneDelo(Delo d) {
		String caseNumber = null;
		try {
			if (checkDelo(d)) {
				caseNumber = d.getCaseNumber();

				String fileName = getDirNameForDocuments(d);
				Path fileXls = Paths.get(xlsDir, fileName + ".xls");
				Path pdfDir = Paths.get(xlsDir, fileName);
				Files.createDirectories(pdfDir);
				createDelo(d, fileXls, pdfDir);

				updateInfo("Создано дело с номером " + caseNumber);
				++stat.casesCreated;
			}
		} catch (WrongPdfFile | ErrorCreateXlsFile wex) {
			updateInfo(wex.getMessage());
		} catch (IOException ex) {
			updateInfo("Не могу создать директорию для дела " + caseNumber + ": " + ex.getMessage());
		}
	}

	/**
	 * Создает файл с описью для нескольких дел
	 */
	private boolean createOpis(GroupDeloKey key, List<Delo> delos) throws WrongPdfFile, IOException {
		Workbook wb = new HSSFWorkbook();

		CellStyle dateStyle = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		dateStyle.setDataFormat(df.getFormat("m/d/yy"));

		Sheet sheet = wb.createSheet("Дело");
		setHeaders(Config.deloHeaders, wb, sheet);
		int size = delos.size();
		int row = 1;
		String xlsFileName = key.toString() + ".xls";
		for (int i = 0; i < size; ++i) {
			if (commit) {
				return false;
			}
			++stat.cases;
			Delo d = delos.get(i);
			if (checkDelo(d)) {
				String caseNumber = d.getCaseNumber();
				Path pdfDir = Paths.get(xlsDir, getDirNameForDocuments(d));
				try {
					Files.createDirectories(pdfDir);
					fillDeloSheet(wb, sheet, d, row);

					Sheet docSheet = wb.createSheet("Документы" + row);
					String caseGraph = d.getCaseGraph();
					int docRowNumber = 1;
					if (caseGraph != null && !caseGraph.trim().isEmpty()) {
						docRowNumber = fillDocsSheet(wb, docSheet, docRowNumber,
								Arrays.<Document>asList(new Document(
												d.getCaseNumber(), d.getEndDate(),
												"Сканобраз обложки бумажного дела",
												caseGraph,
												"Сканобраз обложки бумажного дела"
										)),
								pdfDir, dateStyle);
					}

					fillDocsSheet(wb, docSheet, docRowNumber, d.getDocuments(), pdfDir, dateStyle);
					++row;
					updateInfo("Создано дело с номером " + caseNumber);
					++stat.casesCreated;
				} catch (IOException ex) {
					throw new IOException("Не могу создать директорию для дела " + caseNumber + ": " + ex.getMessage());
				}
			}
		}
		try (OutputStream ous = Files.newOutputStream(Paths.get(xlsDir, xlsFileName))) {
			wb.write(ous);
		} catch (IOException ex) {
			updateInfo(String.format("Не могу создать файл %s: %s", xlsFileName, ex.getMessage()));
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
	private void createDelo(Delo d, Path file, Path pdfDir) throws WrongPdfFile, ErrorCreateXlsFile {

		Workbook wb = new HSSFWorkbook();
		Sheet sheet = wb.createSheet("Дело");
		setHeaders(Config.deloHeaders, wb, sheet);
		fillDeloSheet(wb, sheet, d, 1);

		CellStyle dateStyle = wb.createCellStyle();
		DataFormat df = wb.createDataFormat();
		dateStyle.setDataFormat(df.getFormat("m/d/yy"));

		sheet = wb.createSheet("Документы");
		String caseGraph = d.getCaseGraph();
		int docRowNumber = 1;
		if (caseGraph != null && !caseGraph.trim().isEmpty()) {
			docRowNumber = fillDocsSheet(wb, sheet, docRowNumber,
					Arrays.<Document>asList(new Document(
									d.getCaseNumber(), d.getEndDate(),
									"Сканобраз обложки бумажного дела",
									caseGraph,
									"Сканобраз обложки бумажного дела"
							)),
					pdfDir, dateStyle);
		}

		fillDocsSheet(wb, sheet, docRowNumber, d.getDocuments(), pdfDir, dateStyle);

		writeData(wb, file);
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
	private int fillDocsSheet(Workbook wb, Sheet sheet, int rowNumber, List<Document> documents,
			Path pdfDir, CellStyle dateStyle) throws WrongPdfFile {
		setHeaders(Config.docHeaders, wb, sheet);

		int size = documents.size();
		for (int i = 0; i < size; ++i) {
			Document doc = documents.get(i);
			createDocRecord(sheet.createRow(rowNumber++), doc, dateStyle, pdfDir);
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
	private void createDocRecord(Row row, Document doc, CellStyle style, Path pdfDir) throws WrongPdfFile {
		setCellValue(row.createCell(0), doc.getDocNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), doc.getDocDate(), ValueType.CALENDAR1, style);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4).setCellValue(doc.getDocTitle());
		row.createCell(5);
		row.createCell(6);

		Cell pagesCell = row.createCell(7);
		pagesCell.setCellType(Cell.CELL_TYPE_NUMERIC);
		setCellValue(row.createCell(8), doc.getDocRemark(), ValueType.STRING);
		Cell linkCell = row.createCell(9);
		linkCell.setCellType(Cell.CELL_TYPE_STRING);

		createGraphDoc(doc.getPrikGraph(), pdfDir, pagesCell, linkCell);

		String dopGraph = doc.getDopGraph();
		if (dopGraph != null) {
			dopGraph = dopGraph.trim();
			if (!dopGraph.isEmpty()) {
				createGraphDoc(dopGraph, pdfDir, pagesCell, linkCell);
			}
		}
		setCellValue(row.createCell(10), doc.getDocType(), ValueType.STRING);
		row.createCell(11);
		row.createCell(12);
		row.createCell(13).setCellValue(doc.getStartPage());
	}

	/**
	 * Копирует pdf файлы документа в нужное место и записывает данные о
	 * документе в лист "Документы". в ячейке страниц прописывается суммарное
	 * значение всех графических образов этого документа.
	 *
	 * @param graphLink ссылка на pdf
	 * @param pdfDir директория, относительно которой записываются pdf файлы
	 * @param pagesCell ячейка для записи кол-ва страниц
	 * @param linkCell ячейка для записи путей к файлам, разделенных точкой с
	 * запятой
	 */
	private void createGraphDoc(String graphLink, Path pdfDir, Cell pagesCell, Cell linkCell) throws WrongPdfFile {
		Path srcFile = getPathForLink(graphLink);
		int pages = getPagesOfPdf(srcFile.toString());

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

		String paths = linkCell.getStringCellValue();
		if (paths != null && !paths.trim().isEmpty()) {
			paths += ";" + relativeDstFileName;
		} else {
			paths = relativeDstFileName;
		}
		linkCell.setCellValue(paths);
		pagesCell.setCellValue((int) pagesCell.getNumericCellValue() + pages);
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
	private int getPagesOfPdf(String filename) throws WrongPdfFile {
		try {
			PdfReader reader = new PdfReader(filename);
			int pages = reader.getNumberOfPages();
			reader.close();
			return pages;
		} catch (IOException ex) {
			throw new WrongPdfFile("Невозможно получить кол-во страниц " + filename + ": " + ex.getMessage());
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

	/**
	 * Проверяет правильность оформления дела и его документов
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

		for (Document doc : delo.getDocuments()) {
			Set<ConstraintViolation<Document>> docErrors = validator.validate(doc);
			if (!docErrors.isEmpty()) {
				valid = false;
				if (builder == null) {
					builder = new StringBuilder(String
							.format("Дело с ID = %d имеет следующие ошибки:\n", delo.getId()));
				}
				builder.append("\t").append("документ с ID = ").append(doc.getId())
						.append(" имеет следующие ошибки:\n");
				for (ConstraintViolation<Document> error : docErrors) {
					builder.append("\t\t").append(error.getMessage());
				}
			}
		}
		if (builder != null) {
			updateInfo(builder.toString());
		}
		return valid;
	}
}
