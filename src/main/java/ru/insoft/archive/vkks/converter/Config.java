package ru.insoft.archive.vkks.converter;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Общий настройечный класс для всего приложения.
 *
 * @author Благодатских С.
 */
public class Config {

	public static final String[] deloHeaders = {
		"Индекс дела",
		"Заголовок дела",
		"№ тома",
		"№ части",
		"Дата дела с",
		"Дата дела  по",
		"Примечание"
	};
	
	public static final String FILE_NAME_1 = "111.1. Проблемы преступности";
	public static final String FILE_NAME_2 = "111.2. Проблемы преступности";
	private static final String mode_template = "формировать '%s.xls'";

	public static final String MODE_1 = String.format(mode_template, FILE_NAME_1);
	public static final String MODE_2 = String.format(mode_template, FILE_NAME_2);

	public static final Map<String, String> modeTitles = new HashMap<>();
	public static final Map<String, String> modeNumbers = new HashMap<>();

	public static final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
	public static final String[] docHeaders = {
		"№ регистрации",
		"Дата регистрации",
		"Исходящий №",
		"Дата исходящего",
		"Краткое содержание",
		"Состав",
		"Гриф доступа",
		"Количество листов",
		"Примечание",
		"Файлы",
		"Наименование вида",
		"Вид документа",
		"Том №",
		"Страница №"
	};

	public static final String dbPrefix = "jdbc:ucanaccess://";

	public static final String INIT_DIR_KEY = "workingDirecotry";

	static {
		// Создается чтобы были инициализированы статические поля
		instance = new Config();
		modeTitles.put(MODE_1, "%Проблемы преступности в капиталистических странах (по материалам зарубежной печати)%");
		modeTitles.put(MODE_2, "%Проблемы преступности в капиталистических странах (по материалам иностранной печати)%");
		
		modeNumbers.put(MODE_1, "111.1");
		modeNumbers.put(MODE_2, "111.2");
	}

	private static final Config instance;

	private Config() {
	}
}
