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
 * для режима MATERIALS("1-1-4", Arrays.asList(79217))
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class MaterialsService extends ByBarcodService {

	public MaterialsService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	/**
	 * Формируем сведения о документах дела:
	 */
	/*
	 № регистрации = 1
	 Дата регистрации = \Delo\Date_end
	 Краткое содержание = \Document\Doc_title
	 Количество листов = считать количество страниц в прикрепленном PDF-файле \Document\Graph
	 Файлы = \Document\Graph
	 Наименование вида = «Материалы»
	 Если к записи о деле был прикреплен сканобраз, то прикрепить его к записи о документе, т.е.
	 Файлы = \Document\Graph и \Delo\Graph_delo

	 79598__
	 № регистрации = б/н
	 Дата регистрации = \Document\Date_doc
	 Краткое содержание = \Document\Doc_title
	 Количество листов = считать количество страниц в прикрепленном PDF-файле \Document\Graph
	 Файлы = \Document\Graph
	 Наименование вида = «Брошюра»
	 */
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		boolean secondCase = delo.getBarCode().equals(79598);
		for (Document doc : delo.getDocuments()) {
			String regNumber;
			Calendar date;
			String vidName;
			String pdfLink = getPdfLink(doc.getGraph());
			String graph = pdfLink;
			if (secondCase) {
				regNumber = "б/н";
				date = doc.getDate();
				vidName = "Брошюра";
			} else {
				regNumber = "1";
				date = delo.getEndDate();
				vidName = "Материалы";
				String deloPdfLink = getPdfLink(delo.getGraph());
				if (createTitle(deloPdfLink)) {
					graph += ";" + deloPdfLink;
				}
			}
			documents.add(new XLSDocument(regNumber, date, doc.getTitle(),
					countPages(pdfLink), graph, vidName));
		}
		return documents;
	}

	@Override
	/**
	 * Формируем сведения о томе дела
	 */
	/*
	 Индекс дела = «1-1-4» 
	 Заголовок дела =  \Delo\Delo_title 
	 № тома = 1 
	 Дата дела с = \Delo\Date_start 
	 Дата дела по = \Delo\Date_end
	 */
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-4", delo.getTitle(), 1, delo.getStartDate(),
				delo.getEndDate());
	}

}
