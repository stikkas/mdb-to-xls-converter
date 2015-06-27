package ru.insoft.archive.vkks.converter.dto;

import java.util.Calendar;

/**
 * Сущность дела для передачи данных в XLS файл на лист Дело
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class XLSDelo {

	/**
	 * "Индекс дела",
	 */
	private final String index;
	/**
	 * "Заголовок дела",
	 */
	private final String title;
	/**
	 * "№ тома",
	 */
	private final Integer tomNumber;
	/**
	 * "№ части",
	 */
	private final Integer partNumber;
	/**
	 * "Дата дела с",
	 */
	private final Calendar startDate;
	/**
	 * "Дата дела по",
	 */
	private final Calendar endDate;
	/**
	 * "Примечание"
	 */
	private final String remark;

	public XLSDelo(String index, String title, Integer tomNumber, Calendar startDate,
			Calendar endDate) {
		this(index, title, tomNumber, null, startDate, endDate, "");
	}

	public XLSDelo(String index, String title, Integer tomNumber, Integer partNumber,
			Calendar startDate, Calendar endDate, String remark) {
		this.index = index;
		this.title = title;
		this.tomNumber = tomNumber;
		this.partNumber = partNumber;
		this.startDate = startDate;
		this.endDate = endDate;
		this.remark = remark;
	}

	public String getIndex() {
		return index;
	}

	public String getTitle() {
		return title;
	}

	public Integer getTomNumber() {
		return tomNumber;
	}

	public Integer getPartNumber() {
		return partNumber;
	}

	public Calendar getStartDate() {
		return startDate;
	}

	public Calendar getEndDate() {
		return endDate;
	}

	public String getRemark() {
		return remark;
	}

}
