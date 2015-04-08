package ru.insoft.archive.vkks.converter;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Благодатских С.
 */
public interface Config {

	Map<String, String> caseXlsDbColumns = new HashMap<String, String>() {
		{
			put("Индекс дела", "caseNumIndex");
			put("Даты дела с", "caseDateStart");
			put("Даты дела по", "caseDateEnd");
			put("Заголовок дела", "caseTitle");
			put("№ Тома", "volumeNum");
			put("Примечание", "remark");
		}
	};
	String CASE_TABLE_NAME = "Cases";
	String DOC_TABLE_NAME = "Docs";
}
