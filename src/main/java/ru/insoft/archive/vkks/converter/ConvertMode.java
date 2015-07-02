package ru.insoft.archive.vkks.converter;

/**
 * Представляет различные режима работы конвертора
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public enum ConvertMode {

	CRIME_CAPITAL("Дела рубрики 1-1-6. Проблемы преступности в капиталистических странах");

	private final String displayName;

	private ConvertMode(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
