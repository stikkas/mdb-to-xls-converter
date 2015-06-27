package ru.insoft.archive.vkks.converter;

import java.util.Arrays;
import java.util.List;

/**
 * Представляет различные режима работы конвертора
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public enum ConvertMode {

	CRIME_ZARUB("1-1-6 Проблемы преступности (по материалам зарубежной печати)",
			"%Проблемы преступности в капиталистических странах (по материалам зарубежной печати)%"),
	CRIME_INOSTR("1-1-6 Проблемы преступности (по материалам иностранной печати)",
			"%Проблемы преступности в капиталистических странах (по материалам иностранной печати)%"),
	LAW_STAT("1-4-регл_судебная статистика", Arrays.asList(79552, 79551)),
	METRIC_STAT_BIN("1-1-5-сборник показателей статотчетности", Arrays.asList(79622, 79533, 79538, 79540, 79548, 79614)),
	REVIEW_REPORT("1-1-1-обзоры_доклады", Arrays.asList(79558)),
	CRIME_STATUS_RU("1-1-2-МВД. Состояние преступности", "%Состояние преступности в России%"),
	CRIME_AND_DELICT("1-1-7-СД-Преступность и правонарушения", "%Преступность и правонарушения%"),
	INSTRUCTIONS("1-2-2-Инструкции", "%Инструкция%"),
	ORDERS("1-2-2-Приказы", "%Приказ%"),
	PUBLICATIONS("1-3-Публикации", Arrays.asList(79611));

	private final String searchCriteria;
	private final String displayName;
	private final List<Integer> barCodes;

	private ConvertMode(String displayName, String searchCriteria) {
		this.displayName = displayName;
		this.searchCriteria = searchCriteria;
		barCodes = null;
	}

	private ConvertMode(String displayName, List<Integer> barCodes) {
		this.displayName = displayName;
		this.barCodes = barCodes;
		searchCriteria = null;
	}

	public String getSearchCriteria() {
		return searchCriteria;
	}

	public List<Integer> getBarCodes() {
		return barCodes;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
