package ru.insoft.archive.vkks.converter;

/**
 * Режимы работы конвертора:
 * <ol>
 * <li>Конвертировать одно дело по заданному ID</li>
 * <li>Конвертировать все дела, создавая для каждого дела отдельный xls файл</li>
 * <li>Конвертировать все дела, создавая для группы томов дела одного года один xls файл</li>
 * </ol>
 * @author stikkas<stikkas@yandex.ru>
 */
public enum WorkMode {
	BY_ID,
	CASE_XLS,
	GROUP_CASE_XLS
}
