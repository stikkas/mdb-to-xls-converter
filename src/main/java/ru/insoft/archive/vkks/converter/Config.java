package ru.insoft.archive.vkks.converter;

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
	}

	private static final Config instance;

	private Config() {
	}
}
