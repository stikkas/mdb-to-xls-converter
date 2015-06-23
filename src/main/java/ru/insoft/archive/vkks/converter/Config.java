package ru.insoft.archive.vkks.converter;

import java.text.SimpleDateFormat;

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
	public static final String ONE_VOLUME = "формировать XLS файл для одного тома дела";
	public static final String ONE_CASE_YEAR = "формировать XLS файлы с томами по делу для указанного года";
	public static final String CASES_YEAR = "формировать XLS файлы с делами по подразделению для указанного года";
	public static final String D_TYPE_INNER_OPIS = "Внутренняя опись";
	public static final String D_TYPE_LIST_ZAV = "Лист-заверитель";

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
	}

	private static final Config instance;

	private Config() {
	}
}
