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
 * для режима SVEDENIA("1-1-2-Сведения", "%сведения%", Arrays.asList(79610));
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class SvedeniaService extends LikeTitleExludeBarcodesService {

	public SvedeniaService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	/**
	 * Формируем сведения о документе дела:
	 */
	/*
	 № регистрации = «б/н»
	 Дата регистрации = \Delo\Date_end
	 Краткое содержание = \Delo\Delo_title
	 Количество листов = считать количество страниц в PDF-файле из записи о документе (\Document\Graph), соответствующей делу 
	 Файлы = \Delo\Graph_delo и файл (\Document\Graph) из записи о документе, соответствующей делу 
	 Наименование вида = «Материалы»
	 */
	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		String deloPdfLink = getPdfLink(delo.getGraph());
		for (Document doc : delo.getDocuments()) {
			String docPdfLink = getPdfLink(doc.getGraph());
			documents.add(new XLSDocument("б/н", delo.getEndDate(), delo.getTitle(),
					countPages(docPdfLink), deloPdfLink + ";" + docPdfLink, "Материалы"));
		}
		if (documents.isEmpty()) { // Документов нет
			documents.add(new XLSDocument("б/н", delo.getEndDate(), delo.getTitle(),
					countPages(deloPdfLink), deloPdfLink, "Материалы"));
		}
		return documents;
	}

	/**
	 * Формируем сведения о томе дела
	 */
	/*
	 Индекс дела = «1-1-2»
	 Заголовок дела = \Delo \Delo_title
	 № тома = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end
	 */
	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-2", delo.getTitle(), 1, delo.getStartDate(),
				delo.getEndDate());
	}

}
