package ru.insoft.archive.vkks.converter.dto;

/**
 * Сущность для передачи данных в XLS файл на лист Документы
 * @author stikkas<stikkas@yandex.ru>
 */
public class XLSDocument {

	public XLSDocument(String reportFormNumber, Calendar date, String title,
			String pages, String graph, String remark, String type) {
		this.reportFormNumber = reportFormNumber;
		this.date = date;
		this.title = title;
		this.pages = pages;
		this.graph = graph;
		this.remark = remark;
		this.type = type;
	}

}
