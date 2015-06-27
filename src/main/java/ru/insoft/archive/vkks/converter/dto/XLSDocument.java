package ru.insoft.archive.vkks.converter.dto;

import java.util.Calendar;

/**
 * Сущность для передачи данных в XLS файл на лист Документы
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class XLSDocument {

	/**
	 * "№ регистрации",
	 */
	private final String regNumber;
	/**
	 * "Дата регистрации",
	 */
	private final Calendar regDate;
	/**
	 * "Исходящий №",
	 */
	private final String issueNumber;
	/**
	 * "Дата исходящего",
	 */
	private final Calendar issueDate;
	/**
	 * "Краткое содержание",
	 */
	private final String shortContent;
	/**
	 * "Состав",
	 */
	private final String composition;
	/**
	 * "Гриф доступа",
	 */
	private final String accessGriph;
	/**
	 * "Количество листов",
	 */
	private final Integer pages;
	/**
	 * "Примечание",
	 */
	private final String remark;
	/**
	 * "Файлы",
	 */
	private final String files;
	/**
	 * "Наименование вида",
	 */
	private final String vidName;
	/**
	 * "Вид документа",
	 */
	private final String docVid;
	/**
	 * "Том №",
	 */
	private final Integer tomNumber;
	/**
	 * "Страница №"
	 */
	private final Integer pageNumber;

	public XLSDocument(String regNumber, Calendar regDate, String issueNumber,
			Calendar issueDate, String shortContent, String composition,
			String accessGriph, Integer pages, String remark, String files,
			String vidName, String docVid, Integer tomNumber, Integer pageNumber) {
		this.regNumber = regNumber;
		this.regDate = regDate;
		this.issueNumber = issueNumber;
		this.issueDate = issueDate;
		this.shortContent = shortContent;
		this.composition = composition;
		this.accessGriph = accessGriph;
		this.pages = pages;
		this.remark = remark;
		this.files = files;
		this.vidName = vidName;
		this.docVid = docVid;
		this.tomNumber = tomNumber;
		this.pageNumber = pageNumber;
	}

	public XLSDocument(String regNumber, Calendar regDate, String shortContent,
			Integer pages, String files, String vidName) {
		this(regNumber, regDate, "", null, shortContent, "", "", pages, "", files,
				vidName, "", null, null);
	}

	public XLSDocument(String regNumber, Calendar regDate, String shortContent,
			Integer pages, String remark, String files, String vidName) {
		this(regNumber, regDate, "", null, shortContent, "", "", pages, remark, files,
				vidName, "", null, null);
	}

	public String getRegNumber() {
		return regNumber;
	}

	public Calendar getRegDate() {
		return regDate;
	}

	public String getIssueNumber() {
		return issueNumber;
	}

	public Calendar getIssueDate() {
		return issueDate;
	}

	public String getShortContent() {
		return shortContent;
	}

	public String getComposition() {
		return composition;
	}

	public String getAccessGriph() {
		return accessGriph;
	}

	public Integer getPages() {
		return pages;
	}

	public String getRemark() {
		return remark;
	}

	public String getFiles() {
		return files;
	}

	public String getVidName() {
		return vidName;
	}

	public String getDocVid() {
		return docVid;
	}

	public Integer getTomNumber() {
		return tomNumber;
	}

	public Integer getPageNumber() {
		return pageNumber;
	}

}
