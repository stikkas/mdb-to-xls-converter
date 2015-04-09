package ru.insoft.archive.vkks.converter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.scene.control.TextArea;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ru.insoft.archive.vkks.converter.error.CaseNotFound;
import ru.insoft.archive.vkks.converter.error.WrongFormat;

/**
 * Обрабатывает xls файлы, сопоставляя их с данными в базе. Также копирует pdf
 * файлы из папки с базой в папку с xls файлами.
 *
 * @author Благодатских С.
 */
public class Worker extends Thread {

	public Worker(String xlsDir, String accessDb, TextArea logPanel, List<String> dirsInWork) {
		this.xlsDir = xlsDir;
		this.accessDb = accessDb;
		this.logPanel = logPanel;
		this.dirsInWork = dirsInWork;
		accessDbDir = Paths.get(accessDb).getParent().toString();
		xlsDirPath = Paths.get(xlsDir);
	}

	@Override
	public void run() {
		try {
			Config.dataSource.setUrl(Config.dbPrefix + accessDb);
			connection = Config.dataSource.getConnection();
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(xlsDir));
			updateInfo("Обрабатываю данные в директории " + xlsDir);
			stream.forEach(e -> {
				String fileName = e.getFileName().toString();
				if (fileName.toLowerCase().endsWith(".xls")) {
					currentDirForFile = fileName.substring(0, fileName.length() - 4);
					updateFile(e);
				}
			});
		} catch (IOException ex) {
			updateInfo("Не могу открыть директорию с xls файлами (" + xlsDir + "): " + ex.getMessage());
		} catch (SQLException ex) {
			updateInfo("Не могу прочитать файл базы данных: " + ex.getMessage());
		} finally {
			dirsInWork.remove(xlsDir);
		}
	}

	/**
	 * Обновляет xls файл.
	 * <p>
	 * Один файл содержит два листа:
	 * <ol>
	 * <li>Дело</li>
	 * <li>Документы</li>
	 * </ol></p>
	 * <p>
	 * Лист "Дело" имеет на первой строке:</p>
	 * <table border=1>
	 * <tr>
	 * <th>Индекс дела</th>
	 * <th>Заголовок дела</th>
	 * <th>№ тома</th>
	 * <th>№ части</th>
	 * <th>Дата дела с</th>
	 * <th>Дата дела по	</th>
	 * <th>Примечание</th>
	 * </tr>
	 * </table>
	 * <p>
	 * На второй строке находятся данные по делу. Каждый файл содержит только
	 * одно дело</p>
	 * <p>
	 * MIN_PRIORITY Лист "Документы" имеет на первой строке:</p>
	 * <table border=1>
	 * <tr>
	 * <th>№ регистрации</th>
	 * <th>Дата регистрации</th>
	 * <th>Исходящий №</th>
	 * <th>Дата исходящего</th>
	 * <th>Краткое содержание</th>
	 * <th>Состав</th>
	 * <th>Гриф доступа</th>
	 * <th>Количество листов</th>
	 * <th>Примечание</th>
	 * <th>Файлы</th>
	 * <th>Наименование вида</th>
	 * <th>Вид документа</th>
	 * <th>Том №</th>
	 * <th>Страница №</th>
	 * </tr>
	 * </table>
	 * <p>
	 * Каждая следующая строка предоставляет собой данные по одному документу.
	 * Первая пустая строка (точнее первая пустая колонка какого-то ряда)
	 * считается концом листа</p>
	 *
	 * @param path путь к файлу
	 */
	private void updateFile(Path path) {
		updateInfo("Разбор данных из файла " + path);

		Workbook wb;

		try (InputStream is = Files.newInputStream(path)) {
			wb = new HSSFWorkbook(is);
		} catch (IOException ex) {
			updateInfo("Ошибка при чтении файла " + path + ": " + ex.getMessage());
			wb = null;
		}

		if (wb != null) {
			boolean errors = false;
			try {
				updateDocs(wb.getSheet("Документы"), getCaseId(wb.getSheet("Дело")));
				OutputStream out = Files.newOutputStream(path);
				wb.write(out);
				out.close();
			} catch (WrongFormat ex) {
				updateInfo("Ошибка обработки файла " + path + ": " + ex.getMessage());
				errors = true;
			} catch (SQLException ex) {
				updateInfo("Ошибка базы данных: " + ex.getMessage());
				errors = true;
			} catch (CaseNotFound ex) {
				updateInfo("Данные по делу '" + ex.getCaseNumber() + "' отсутсвуют");
				errors = true;
			} catch (IOException ex) {
				updateInfo("Ошибка при записи файла " + path + ": " + ex.getMessage());
				errors = true;
			}
			showResultForFileInfo(path, errors);
		}
	}

	private final String xlsDir;
	private final Path xlsDirPath;
	private final String accessDb;
	private final List<String> dirsInWork;
	private final TextArea logPanel;
	private Connection connection;
	private final String accessDbDir;
	private String currentDirForFile;
	private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");

	private void showResultForFileInfo(Path path, boolean errors) {
		updateInfo("================================\n"
				+ "Файл " + path + " обработан " + (errors ? " с ошибками." : " без ошибок."));
	}

	/**
	 * Получает индексы колонок с интересующими названиями
	 */
	private boolean getColumnIndeces(Sheet sheet, Map<String, Short> result) {
		Row header = sheet.getRow(0);
		short lastIndexCol = header.getLastCellNum();
		short i = header.getFirstCellNum();
		int found = 0;
		for (; i <= lastIndexCol; ++i) {
			String name = header.getCell(i, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
			if (result.containsKey(name)) {
				result.put(name, i);
				++found;
			}
		}
		return found == result.size();
	}

	/**
	 * Возвращает идентификатор дела из базы, орентируясь на данные из xls
	 * файла.
	 *
	 * @param sheet лист с делом
	 * @return идентификатор дела
	 */
	private Long getCaseId(Sheet sheet) throws WrongFormat, CaseNotFound, SQLException {
		Map<String, Short> result = new HashMap<>(Config.caseXlsDbColumns.size());
		Config.caseXlsDbColumns.keySet().stream().forEach(st -> {
			result.put(st, null);
		});

		if (!getColumnIndeces(sheet, result)) {
			throw new WrongFormat("На листе дела отсутствуют заголовки:" + getWrongHeaders(result));
		}

		Row row = sheet.getRow(1);
		PreparedStatement st = connection.prepareStatement(Config.CASE_ID_QUERY);
		String caseNumber = row.getCell(result.get(Config.CASE_NUM_COL_HEADER), Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		if (caseNumber.isEmpty()) {
			throw new WrongFormat("У дела отсутствует индекс");
		}
		st.setString(1, caseNumber);
		java.util.Date startDate, endDate;
		Cell startCell = row.getCell(result.get(Config.START_DATE_COL_HEADER), Row.CREATE_NULL_AS_BLANK);
		Cell endCell = row.getCell(result.get(Config.END_DATE_COL_HEADER), Row.CREATE_NULL_AS_BLANK);
		try {
			startDate = startCell.getDateCellValue();
			if (startDate == null) {
				throw new WrongFormat("отсутсвует " + Config.START_DATE_COL_HEADER);
			}
			endDate = endCell.getDateCellValue();
			if (endDate == null) {
				throw new WrongFormat("отсутсвует " + Config.END_DATE_COL_HEADER);
			}
		} catch (IllegalStateException ex) {
			startDate = getStartEndDate(Config.START_DATE_COL_HEADER, startCell);
			endDate = getStartEndDate(Config.END_DATE_COL_HEADER, endCell);
		}

		st.setDate(2, new Date(startDate.getTime()));
		st.setDate(3, new Date(endDate.getTime()));

		ResultSet rs = st.executeQuery();
		if (rs.next()) {
			return rs.getLong(1);
		}
		throw new CaseNotFound(caseNumber);
	}

	/**
	 * Определяет какие заголовки не найдены на листе
	 *
	 * @param headers список необходимых заголовков
	 * @return строку с пустыми заголовками
	 */
	private String getWrongHeaders(Map<String, Short> headers) {
		StringBuilder builder = new StringBuilder();
		headers.keySet().stream().filter((key) -> (headers.get(key) == null)).forEach((key) -> {
			builder.append(" '").append(key).append("' ");
		});
		return builder.toString();
	}

	/**
	 * Получает дату из ячейки, в которой дата записана строкой
	 *
	 * @param fieldHeader название столбца
	 * @param cell ячейчка с данными
	 * @return дату
	 * @throws WrongFormat
	 */
	private java.util.Date getStartEndDate(String fieldHeader, Cell cell) throws WrongFormat {
		String data = cell.getStringCellValue();
		java.util.Date date = null;
		try {
			date = sdf.parse(data);
		} catch (ParseException ex) {
		}
		if (date == null) {
			throw new WrongFormat(fieldHeader + " имеет неправильный формат");
		}
		return date;
	}

	/**
	 * Обновляет данные по документам (вставляет в ячейку "Файлы" путь к pdf).
	 *
	 * @param docs - лист с информацией по документам
	 * @param id - идентификатор дела (из базы)
	 */
	private void updateDocs(Sheet docs, Long id) throws WrongFormat, SQLException {
		Map<String, Short> result = new HashMap<>(Config.docXlsColumnNames.length);
		for (String s : Config.docXlsColumnNames) {
			result.put(s, null);
		}
		if (!getColumnIndeces(docs, result)) {
			throw new WrongFormat("На листе документов отсутсвуют заголовки:" + getWrongHeaders(result));
		}

		int rows = docs.getLastRowNum();
		PreparedStatement st = connection.prepareStatement(Config.DOC_FILE_QUERY);

		short regNumColIdx = result.get(Config.DOC_REG_NUM);
		short filesNumColIdx = result.get(Config.DOC_FILES);
		short typeDocNumColIdx = result.get(Config.DOC_TYPE);

		for (int i = 1; i <= rows; ++i) {
			Row row = docs.getRow(i);
			String regNumber = row.getCell(regNumColIdx).getStringCellValue();

			st.setLong(1, id);
			st.setString(2, regNumber);
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				String fileLink = rs.getString(1);
				if (fileLink.isEmpty()) {
					updateInfo("В базе данных отсутствует ссылка на файл для документа " + regNumber);
					continue;
				}
				Path srcFile = getPathForLink(fileLink);
				Path dstDir = getDstDir(row.getCell(
						typeDocNumColIdx, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
				if (dstDir == null) {
					continue;
				}
				Path dstFile = dstDir.resolve(srcFile.getFileName());
				if (!Files.exists(dstFile)) {
					try {
						Files.copy(srcFile, dstFile);
//						updateInfo("Файл " + srcFile + " скопирован в " + dstDir);
					} catch (IOException ex) {
						updateInfo("Ошибка копирования файла " + srcFile + " в "
								+ dstDir + ": " + ex.getMessage());
						continue;
					}
				} else {
					updateInfo("Файл " + dstFile + " уже существует");
				}
				String relativeDstFileName = FilenameUtils.separatorsToWindows(
						dstFile.relativize(xlsDirPath).toString());
				Cell filesCell = row.getCell(filesNumColIdx);
				String filesData = filesCell.getStringCellValue();
				if (filesData.isEmpty()) {
//					filesCell.setCellValue(relativeDstFileName);
//					updateInfo("Для документа " + regNumber + " создана запись в поле 'Файлы'");
					updateInfo("Имитация " + relativeDstFileName);
				} else if (!filesData.contains(relativeDstFileName)) {
//					filesCell.setCellValue(filesData + ";" + relativeDstFileName);
//					updateInfo("Для документа " + regNumber + " добавлена запись в поле 'Файлы'");
					updateInfo("Имитация " + relativeDstFileName);
				} else {
					updateInfo("Для документа " + regNumber + " запись в поле 'Файлы' не изменена");
				}
			} else {
				updateInfo("Документ " + regNumber + " не найден");
			}
		}
	}

	/**
	 * Возвращает директорию, куда нужно поместить файл. В xls файле путь
	 * представлен в Windows формате.
	 *
	 * @param typeDoc вид документа, отсюда определяется относительный путь,
	 * если присутствует
	 * @return путь назначения
	 */
	private Path getDstDir(String typeDoc) {
		if (typeDoc.isEmpty()) {
			try {
				Path p = Paths.get(currentDirForFile);
				Files.createDirectories(p);
				return p;
			} catch (IOException ex) {
				updateInfo("Ошибка создания директории " + currentDirForFile
						+ ": " + ex.getMessage());
				return null;
			}
		}
		return Paths.get(xlsDir, Paths.get(FilenameUtils.separatorsToSystem(typeDoc)).toFile().getParent());
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
	 * Пишет информацию о ходе выполнения в статустую панель.
	 *
	 * @param message сообщение
	 */
	private void updateInfo(String message) {
		logPanel.insertText(0, message + "\n");
	}
}
