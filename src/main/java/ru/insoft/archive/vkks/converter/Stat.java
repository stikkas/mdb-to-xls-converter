package ru.insoft.archive.vkks.converter;

/**
 * Класс для сбора статистики по обработтаным записям mdb файла
 *
 * @author stikkas<stikkas@yandex.ru>
 */
class Stat {

	/**
	 * Кол-во обработанных дел
	 */
	long cases;
	/**
	 * Кол-во созданных записей документов
	 */
	long docs;
	/**
	 * Кол-во созданных файлов дел
	 */
	long casesCreated;

	@Override
	public String toString() {
		return String.format("Обработано дел - %d\n"
				+ "Создано дел - %d\nПропущено дел - %d\nЗаписано документов - %d\n",
				cases, casesCreated, cases - casesCreated, docs);
	}

}
