package ru.insoft.archive.vkks.converter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
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
	/**
	 * Может содержать либо идентификатор дела, либо год, зависит от режима
	 */
	private final Integer idOrYear;
	private final String xlsDir;
	private final EntityManager em;
	private final TextArea logPanel;
	private final Stat stat = new Stat();
	private static final javax.validation.Validator validator
			= Validation.buildDefaultValidatorFactory().getValidator();

	public Worker(String accessDb, TextArea logPanel, String mode, Integer idOrYear) {
		this.xlsDir = Paths.get(accessDb).getParent().toString();
		this.logPanel = logPanel;
		this.mode = mode;
		this.idOrYear = idOrYear;

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
			switch (mode) {
				case Config.ONE_VOLUME:
					createForId();
					break;
				case Config.ONE_CASE_YEAR:
					createVolumes();
					break;
				default: // Config.CASES_YEAR
					createUnits();
			}
		} finally {
			updateInfo(stat.toString());
			done.set(true);
		}
	}

	/**
	 * Выбирает из базы дело с заданным ID и формирует для него xls
	 */
	private void createForId() {
		Delo delo = em.find(Delo.class, idOrYear);
		if (delo == null) {
			updateInfo("Дело не найдено");
			return;
		}
		++stat.cases;
		convertOneDelo(delo);
	}

	/**
	 * Формирует файлы для дел за определненный год, сгруппированных по номеру
	 * дела.
	 */
	private void createVolumes() {
		for (Entry<String, List<Delo>> e : ((List<Delo>) em.createQuery("SELECT d FROM Delo d", Delo.class)
				.getResultList()).stream()
				.filter(d -> d.getStartDate().get(Calendar.YEAR) == idOrYear)
				.collect(Collectors.groupingBy(delo -> delo.getCaseNumber()))
				.entrySet()) {

			if (!convertFewDelo(e.getValue(), e.getKey() + "_" + idOrYear)) {
				break;
			}
		}

	}

	/**
	 * Формирует файлы дел за определенный год, сгруппированных по
	 * подразделениям
	 */
	private void createUnits() {
		for (Entry<String, List<Delo>> e : ((List<Delo>) em.createQuery("SELECT d FROM Delo d", Delo.class)
				.getResultList()).stream()
				.filter(d -> d.getStartDate().get(Calendar.YEAR) == idOrYear)
				.collect(Collectors.groupingBy(delo -> {
					String[] parts = delo.getCaseNumber().split("-");
					if (parts.length > 1) {
						List<String> strings = new ArrayList<>();
						for (int i = 0; i < parts.length - 1; ++i) {
							strings.add(parts[i]);
						}
						return String.join("-", strings);
					} else {
						return parts[0];
					}
				}))
				.entrySet()) {
			if (!convertFewDelo(e.getValue(), e.getKey() + "_" + idOrYear)) {
				break;
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
		try {
			if (checkDelo(d)) {
				Workbook wb = new HSSFWorkbook();

				createDelo(d, wb.createSheet("Дело"), wb, 1, true);

				writeData(wb, Paths.get(xlsDir, getXlsFileName(d)));
				updateInfo("Создано дело с номером " + d.getCaseNumber());
				++stat.casesCreated;
			}
		} catch (ErrorCreateXlsFile wex) {
			updateInfo(wex.getMessage());
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
	private boolean convertFewDelo(List<Delo> dela, String fileName) {

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
				updateInfo("Создано дело с номером " + d.getCaseNumber());
				++stat.casesCreated;
			}
		}

		if (!dela.isEmpty()) {
			Delo d = dela.get(0);
			try {
				writeData(wb, Paths.get(xlsDir, fileName + ".xls"));
			} catch (ErrorCreateXlsFile ex) {
				updateInfo(ex.getMessage());
			}
		} else {
			updateInfo("Для заданного года - " + idOrYear + " дела не найдены");
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
		String caseGraph = d.getCaseGraph();
		createHeaders = true;

		int docRowNumber = 1;
		if (caseGraph != null && !caseGraph.trim().isEmpty()) {
			docRowNumber = fillDocsSheet(wb, sheet, docRowNumber,
					Arrays.<Document>asList(new Document(
									d.getCaseNumber(), d.getEndDate(),
									"Сканобраз обложки бумажного дела",
									caseGraph,
									"Сканобраз обложки бумажного дела", d)),
					dateStyle, createHeaders);
			createHeaders = false;
		}
		List<Document> documents = new ArrayList<>();
		List<Document> opisis = new ArrayList<>();
		List<Document> zaveritels = new ArrayList<>();

		// Разбираем документы на просто документы и на листы-заверители и внутренние описи
		for (Document doc : d.getDocuments()) {
			String docType = doc.getDocType();
			if (docType != null) {
				docType = docType.trim();
				switch (docType) {
					case Config.D_TYPE_INNER_OPIS:
						opisis.add(new Document(d.getCaseNumber() + " том "
								+ d.getTomNumber(),
								d.getEndDate(),
								Config.D_TYPE_INNER_OPIS, doc.getPrikGraph(),
								Config.D_TYPE_INNER_OPIS, d));
						break;
					case Config.D_TYPE_LIST_ZAV:
						zaveritels.add(new Document(d.getCaseNumber() + " том "
								+ d.getTomNumber(),
								d.getEndDate(),
								Config.D_TYPE_LIST_ZAV,
								doc.getPrikGraph(),
								Config.D_TYPE_LIST_ZAV, d));
						break;
					default:
						documents.add(doc);
				}
			} else {
				documents.add(doc);
			}
		}

		docRowNumber = fillDocsSheet(wb, sheet, docRowNumber, opisis, dateStyle, createHeaders);
		docRowNumber = fillDocsSheet(wb, sheet, docRowNumber, documents, dateStyle, false);
		fillDocsSheet(wb, sheet, docRowNumber, zaveritels, dateStyle, false);

	}

	/**
	 * Возвращает название файла xls для записи конвертированных данных
	 *
	 * @param d дело
	 * @return
	 */
	private String getXlsFileName(Delo d) {
		String fileName = d.getCaseNumber() + "_"
				+ Config.sdf.format(d.getStartDate().getTime()) + "-"
				+ Config.sdf.format(d.getEndDate().getTime());

		if (createdFileNames.contains(fileName)) {
			int index = 1;
			for (; createdFileNames.contains(fileName + "_" + index); ++index);
			fileName += "_" + index;
			createdFileNames.add(fileName);
		}
		return fileName + ".xls";
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
			CellStyle dateStyle, boolean createHeaders) {
		if (createHeaders) {
			setHeaders(Config.docHeaders, wb, sheet);
		}

		int size = documents.size();
		for (int i = 0; i < size; ++i) {
			Document doc = documents.get(i);
			createDocRecord(sheet.createRow(rowNumber++), doc, documents, i, dateStyle);
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
	private void createDocRecord(Row row, Document doc, List<Document> docs, int index,
			CellStyle style) {
		setCellValue(row.createCell(0), doc.getDocNumber(), ValueType.STRING);
		setCellValue(row.createCell(1), doc.getDocDate(), ValueType.CALENDAR1, style);
		row.createCell(2);
		row.createCell(3);
		row.createCell(4).setCellValue(doc.getDocTitle());
		row.createCell(5);
		row.createCell(6);

		setCellValue(row.createCell(7), countPages(doc, docs, index), ValueType.INTEGER);

		setCellValue(row.createCell(8), doc.getDocRemark(), ValueType.STRING);
		setCellValue(row.createCell(9), getPdfLink(doc.getPrikGraph()), ValueType.STRING);
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
	 * Определяем кол-во страниц документа
	 *
	 * @param doc документ
	 * @param docs документы дела
	 * @param index индекс данного документа среди документов дела
	 * @return количество страниц
	 */
	private int countPages(Document doc, List<Document> docs, int index) {
		if (index == docs.size() - 1) { // Последний документ
			return 1;
		}

		Integer startPage = doc.getNumberPage();
		if (startPage == null) {
			return 1;
		}

		Integer endPage = docs.get(index + 1).getNumberPage();
		if (endPage == null) {
			return 1;
		}
		return endPage - startPage;
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
		} else { // Т.к. в поле id_main может хранится разная чушь, а не только id
			// То приходится писать такой гавнокод.
			for (Document doc : delo.getDocuments()) {
				String mainId = doc.getMainId();
				if (mainId != null && !mainId.trim().isEmpty()) {
					try {
						Integer id = Integer.parseInt(mainId);
						doc.setMain(delo.getDocuments().stream().filter(d -> Objects.equals(d.getId(), id)).findFirst().get());
					} catch (NumberFormatException e) {

					}
				}
			}
		}

		if (builder != null) {
			updateInfo(builder.toString());
		}
		return valid;
	}
}
