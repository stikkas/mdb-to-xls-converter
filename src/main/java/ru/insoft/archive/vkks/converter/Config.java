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
	
	public static final String MODE_1 = "1-1-6 Проблемы преступности (по материалам зарубежной печати)";
	public static final String MODE_2 = "1-1-6 Проблемы преступности (по материалам иностранной печати)";

	public static final Map<String, String> modeTitles = new HashMap<>();

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
	}

	private static final Config instance;

	private Config() {
	}
}
