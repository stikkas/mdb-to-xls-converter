package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.persistence.EntityManager;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class CrimeCapitalService extends Service {

	public CrimeCapitalService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	/**
	 * Для дела формируем в XLS сведения о документах дела
	 */
	/*
	 № регистрации = значение родительского \Delo\Number_tom 
	 Дата регистрации = \Document\Date_doc
	 Краткое содержание = \Document\Doc_title
	 Количество листов = считаем количество страниц в файле PDF
	 Файлы = \Document\Graph 
	 Наименование вида = Ежемесячный информационный бюллетень ВИНИТИ
	 */
	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument>  documents = new ArrayList();
		for (Document doc : delo.getDocuments()) {
			String link = getPdfLink(doc.getGraph());
			documents.add(new XLSDocument(doc.getReportFormNumber(), doc.getDate(), doc.getTitle(), 
					countPages(link), link, "Ежемесячный информационный бюллетень ВИНИТИ"));
		}
		return documents;
	}

	/**
	 * Формируем в XLS cведения о деле:
	 */
	/*
	 Индекс дела = «1-1-6-» + "значение года в формате ХХХХ" (
	 значение года из собранного множества подчиненных \Document\Date_doc, имеющих одинаковый заголовок дела и тождественный год документа)
	 Заголовок дела = \Delo\Delo_title (
	 собранное множество тождественных заголовков дел, имеющих документы с тождественным годом)
	 № тома = 1
	 Дата дела с = "01.01."+«значение года в формате ХХХХ" (
	 значение года из собранного множества подчиненных \Document\Date_doc, имеющих одинаковый заголовок дела и тождественный год документа)
	 Дата дела по = "31.12."+«значение года в формате ХХХХ" (
	 значение года из собранного множества подчиненных \Document\Date_doc, имеющих одинаковый заголовок дела и тождественный год документа)
	 */
	@Override
	public XLSDelo getDelo(Delo delo) {
		int year = delo.getDocuments().get(0).getDate().get(Calendar.YEAR);
		Calendar start = Calendar.getInstance();
		Calendar end = Calendar.getInstance();
		start.set(year, 0, 1);
		end.set(year, 11, 31);
		return new XLSDelo("1-1-6-" + year, delo.getTitle(), 1, start, end);
	}

	@Override
	public List<Delo> getDelos(EntityManager em) {
		return em.createQuery("SELECT d FROM Delo d", Delo.class).getResultList();
	}

}
