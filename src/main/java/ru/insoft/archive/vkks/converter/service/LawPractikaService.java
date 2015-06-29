package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * для режима LAW_PRACTIKA("1-1-7-Судебная практика", Arrays.asList(79610)),
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class LawPractikaService extends ByBarcodService {

	public LawPractikaService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	/**
	 * Формируем сведения о документе дела:
	 *
	 * @return
	 * @throws ru.insoft.archive.vkks.converter.error.WrongPdfFile
	 */
	/*
	 № регистрации = «б/н»
	 Дата регистрации = \Delo\Date_end
	 Краткое содержание = \Delo \Delo_title
	 Количество листов = считать количество страниц в PDF-файле из \Document\Graph
	 Файлы = \Document\Graph
	 Наименование вида = «Материалы» 

	 Файл, прикрепленный к записи о деле (\Delo\Graph_delo) оформить как документ «Титульный лист», расположить первым в списке записей о документах
	 № регистрации = "б/н" 
	 Дата регистрации = \Delo\Date_end 
	 Краткое содержание = "Титульный лист" 
	 Количество листов = считать количество страниц в прикрепленном PDF-файле
	 Файлы = \Delo\Graph_delo 
	 Примечание = is Null
	 Наименование вида = "Титульный лист"
	 */
	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		String pdfLink = getPdfLink(delo.getGraph());
		if (createTitle(pdfLink)) {
			documents.add(new XLSDocument("б/н", delo.getEndDate(), "Титульный лист",
					countPages(pdfLink), pdfLink, "Титульный лист"));
		}
		for (Document doc : delo.getDocuments()) {
			pdfLink = getPdfLink(doc.getGraph());
			documents.add(new XLSDocument("б/н", delo.getEndDate(), delo.getTitle(),
					countPages(pdfLink), pdfLink, "Материалы"));
		}
		return documents;
	}

	/**
	 * Формируем сведения о томе дела
	 *
	 * @return
	 */
	/*
	 Индекс дела = «1-1-7»
	 Заголовок дела = \Delo \Delo_title
	 № тома = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end
	 */
	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-7", delo.getTitle(), 1, delo.getStartDate(),
				delo.getEndDate());
	}

}
