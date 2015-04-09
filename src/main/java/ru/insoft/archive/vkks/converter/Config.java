package ru.insoft.archive.vkks.converter;

import java.util.HashMap;
import java.util.Map;
import javafx.util.Pair;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

/**
 * Общий настройечный класс для всего приложения.
 *
 * @author Благодатских С.
 */
public class Config {

	public static final String CASE_NUM_COL_HEADER = "Индекс дела";
	public static final String START_DATE_COL_HEADER = "Дата дела с";
	public static final String END_DATE_COL_HEADER = "Дата дела  по";

	public static final String DOC_REG_NUM = "№ регистрации";
	public static final String DOC_FILES = "Файлы";
	public static final String DOC_TYPE = "Вид документа";

	public static final String[] docXlsColumnNames = {
		DOC_REG_NUM, DOC_FILES, DOC_TYPE
	};
	public static final Pair<String, String> docXlsDbColumns = new Pair<>(DOC_REG_NUM, "docIndex");

	public static final Map<String, String> caseXlsDbColumns = new HashMap<String, String>() {
		{
			put(CASE_NUM_COL_HEADER, "caseNumIndex");
			put(START_DATE_COL_HEADER, "caseDateStart");
			put(END_DATE_COL_HEADER, "caseDateEnd");
		}
	};

	public static final String CASE_TABLE_NAME = "Cases";
	public static final String DOC_TABLE_NAME = "Docs";
	public static final DriverManagerDataSource dataSource = new DriverManagerDataSource();
	public static final String dbPrefix = "jdbc:ucanaccess://";

	public static final String INIT_DIR_KEY = "workingDirecotry";

	public static final String CASE_ID_QUERY = "SELECT id FROM "
			+ CASE_TABLE_NAME + " WHERE "
			+ caseXlsDbColumns.get(CASE_NUM_COL_HEADER)
			+ "=? AND " + caseXlsDbColumns.get(START_DATE_COL_HEADER)
			+ "=? AND " + caseXlsDbColumns.get(END_DATE_COL_HEADER)
			+ "=?";

	public static final String DOC_FILE_QUERY = "SELECT file FROM "
			+ DOC_TABLE_NAME + " WHERE parent_id=? AND "
			+ docXlsDbColumns.getValue() + "=?";

	static {
		// Создается чтобы были инициализированы статические поля
		instance = new Config();
		dataSource.setDriverClassName("net.ucanaccess.jdbc.UcanaccessDriver");
	}

	private static final Config instance;

	private Config() {
	}
}
