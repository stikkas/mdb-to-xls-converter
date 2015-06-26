package ru.insoft.archive.vkks.converter;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	
	public static final String MODE_1 = "1-4-регл_судебная статистика";
	public static final String MODE_2 = "1-1-5-сборник показателей статотчетности";
	public static final String MODE_3 = "1-1-1-обзоры_доклады";

	public static final Map<String, List<Integer>> modeBarCodes = new HashMap<>();

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
		modeBarCodes.put(MODE_1, Arrays.asList(79552, 79551));
		modeBarCodes.put(MODE_2, Arrays.asList(79622, 79533, 79538, 79540, 79548, 79614));
		modeBarCodes.put(MODE_3, Arrays.asList(79558));
	}

	private static final Config instance;

	private Config() {
	}
}
