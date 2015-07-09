package ru.insoft.archive.vkks.converter;

import java.util.Arrays;
import java.util.List;

/**
 * Представляет различные режима работы конвертора
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public enum ConvertMode {

	ABOUT_FACES("1-1-2-о лицах", "%о лицах%"),
	STATISTIC_DATA("1-1-3-Сборник статистических данных",
			"%Сборник статистических данных о работе судов по рассмотрению гражданских дел: Сроки. Профилактика. Качество%"),
	MAIN_CRITERIAS("1-1-3-Сборник основных показателей", "%Сборник основных показателей%"),
	RABOTA_NAROD_LAW(Arrays.asList(484), "1-4-1-Работа народных судов"),
	CRIME_ZARUB("1-1-6 Проблемы преступности (по материалам зарубежной печати)",
			"%Проблемы преступности в капиталистических странах (по материалам зарубежной печати)%"),
	CRIME_INOSTR("1-1-6 Проблемы преступности (по материалам иностранной печати)",
			"%Проблемы преступности в капиталистических странах (по материалам иностранной печати)%"),
	LAW_STAT("1-4-регл_судебная статистика", Arrays.asList(79552, 79551)),
	METRIC_STAT_BIN("1-1-5-сборник показателей статотчетности", Arrays.asList(79622, 79533, 79538, 79540, 79548, 79614)),
	REVIEW_REPORT("1-1-1-обзоры_доклады", Arrays.asList(79558)),
	CRIME_STATUS_RU("1-1-2-МВД. Состояние преступности", "%Состояние преступности в России%"),
	CRIME_AND_DELICT("1-1-7-СД-Преступность и правонарушения", "%Преступность и правонарушения%"),
	CRIME_AND_DELICT2("1-1-7_Преступность и правонарушения", "%Преступность и правонарушения%"),
	INSTRUCTIONS("1-2-2-Инструкции", "%Инструкция%"),
	ORDERS("1-2-2-Приказы", "%Приказ%"),
	PUBLICATIONS("1-3-Публикации", Arrays.asList(79611)),
	MATERIALS("1-1-4", Arrays.asList(79217, 79598)),
	RECOMENDATIONS("1-2-2-Рекомендации", Arrays.asList(79291)),
	COMPARE_TABLE("1-2-2-Сравнительная таблица статей УК", Arrays.asList(79515)),
	SVEDENIA("1-1-2-Сведения", "%сведения%", Arrays.asList(79610)),
	LAW_PRACTIKA("1-1-7-Судебная практика", Arrays.asList(79610)),
	ANALITIC_TABLES("1-1-3-Аналитические таблицы", Arrays.asList(79525, 79469, 79595)),
	BILLS_REG_FORMS("1-2-1-Бланки регламентных форм", Arrays.asList(79554, 79553));

	private final String searchCriteria;
	private final String displayName;
	private final List<Integer> barCodes;
	private final List<Integer> ids;

	private ConvertMode(String displayName, String searchCriteria, List<Integer> barCodes) {
		this.displayName = displayName;
		this.searchCriteria = searchCriteria;
		this.barCodes = barCodes;
		ids = null;
	}

	private ConvertMode(String displayName, String searchCriteria) {
		this.displayName = displayName;
		this.searchCriteria = searchCriteria;
		barCodes = null;
		ids = null;
	}

	private ConvertMode(String displayName, List<Integer> barCodes) {
		this.displayName = displayName;
		this.barCodes = barCodes;
		searchCriteria = null;
		ids = null;
	}

	private ConvertMode(List<Integer> ids, String displayName) {
		this.displayName = displayName;
		this.ids = ids;
		searchCriteria = null;
		barCodes = null;
	}

	public String getSearchCriteria() {
		return searchCriteria;
	}

	public List<Integer> getBarCodes() {
		return barCodes;
	}

	public List<Integer> getIds() {
		return ids;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
