package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * для режима ANALITIC_TABLES("1-1-3-Аналитические таблицы",
 * Arrays.asList(79525, 79469, 79595)),
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class AnaliticTablesService extends ByBarcodService {

	public AnaliticTablesService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	/**
	 * Формируем сведения о документе дела:
	 *
	 * @param delo
	 * @return
	 * @throws WrongPdfFile
	 */
	/*
	 Файл, прикрепленный к записи о деле (\Delo\Graph_delo) оформить как документ «Титульный лист», расположить первым в списке записей о документах
	 № регистрации = "б/н" 
	 Дата регистрации = 31.12.1983 
	 Краткое содержание = "Титульный лист" 
	 Количество листов = считать количество страниц в прикрепленном PDF-файле
	 Файлы = \Delo\Graph_delo 
	 Примечание = is Null
	 Наименование вида = "Титульный лист"

	 для записей из mdb: у которых \Delo\Case_barcod=79525,79469,
	 № регистрации = \Document\Report_form_number, если \Document\Report_form_number = is Null, то № регистрации = "б\н" 
	 Дата регистрации = преобразовать в "31.12.19"+"\Document\Report_year", если \Document\Report_year = is Null, то Дата регистрации = \Delo\Date_start 
	 Краткое содержание = \Document\Doc_title
	 Количество листов = считать количество страниц в PDF-файле из \Document\Graph
	 Файлы = \Document\Graph
	 Примечание = "Рукописная"+" \Document\Subject_name_RF"+" \Document\Law_court_name"+" \Document\Report_period" 
	 Наименование вида = «Аналитические таблицы»

	 для записи из mdb: у которой \Delo\Case_barcod=79595
	 № регистрации = \Document\Report_form_number, если \Document\Report_form_number = is Null, то № регистрации = "б\н" 
	 Дата регистрации = \Delo\Date_end
	 Краткое содержание = \Document\Doc_title
	 Количество листов = считать количество страниц в PDF-файле из \Document\Graph
	 Файлы = \Document\Graph
	 Примечание = "Рукописная"+" \Document\Subject_name_RF"+" \Document\Law_court_name"+" \Document\Report_period" 
	 Наименование вида = «Аналитические таблицы»
	 */
	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		String pdfLink = getPdfLink(delo.getGraph());
		if (createTitle(pdfLink)) {
			Calendar cal = Calendar.getInstance();
			cal.set(1983, 11, 31);
			documents.add(new XLSDocument("б/н", cal, "Титульный лист",
					countPages(pdfLink), pdfLink, "Титульный лист"));
		}

		boolean secondCase = delo.getBarCode().equals(79595);
		for (Document doc : delo.getDocuments()) {
			pdfLink = getPdfLink(doc.getGraph());
			String regNumber = doc.getReportFormNumber();
			if (!createTitle(regNumber)) {
				regNumber = "б/н";
			}
			Calendar regDate;
			if (secondCase) {
				regDate = delo.getEndDate();
			} else {
				try {
					Integer year = Integer.valueOf(doc.getReportYear());
					regDate = Calendar.getInstance();
					regDate.set(Integer.valueOf(String.format("19%d", year)), 11, 31);
				} catch (NumberFormatException ex) {
					regDate = delo.getStartDate();
				}
			}
			documents.add(new XLSDocument(regNumber, regDate, doc.getTitle(),
					countPages(pdfLink), "Рукописная" + doc.getSubjectNameRF() + " " + doc.getLawCourtName()
					+ doc.getReportPeriod(), pdfLink, "Аналитические таблицы"));
		}
		return documents;
	}

	/**
	 * Формируем сведения о томе дела
	 *
	 * @param delo
	 * @return
	 */
	/*
	 для записей из mdb: у которых \Delo\Case_barcod=79525,79469,
	 Индекс дела = «1-1-3»
	 Заголовок дела = \Delo \Delo_title
	 № тома = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = 31.12.1983

	 для записи из mdb: у которой \Delo\Case_barcod=79595
	 Индекс дела = «1-1-3»
	 Заголовок дела = \Delo \Delo_title
	 № тома = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end
	 */
	@Override
	public XLSDelo getDelo(Delo delo) {
		Calendar endDate;
		if (delo.getBarCode().equals(79595)) {
			endDate = delo.getEndDate();
		} else {
			endDate = Calendar.getInstance();
			endDate.set(1983, 11, 31);
		}
		return new XLSDelo("1-1-3", delo.getTitle(), 1, delo.getStartDate(), endDate);
	}

}
