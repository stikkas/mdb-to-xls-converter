package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * для режима PUBLICATIONS("1-3-Публикации", Arrays.asList(79611));
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class PublicationsService extends Service {

	public PublicationsService(Path workDir) {
		super(workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		for (Document doc : delo.getDocuments()) {
			String pdfLink = getPdfLink(doc.getGraph());
			String graph = pdfLink;
			String deloPdfLink = getPdfLink(delo.getGraph());
			if (createTitle(deloPdfLink)) {
				graph += ";" + deloPdfLink;
			}
			documents.add(new XLSDocument("1", delo.getEndDate(), doc.getTitle(),
					countPages(pdfLink), graph, "Брошюра"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-3 Правовая информатика", "Правовая информатика", 1,
				delo.getStartDate(), delo.getEndDate());
	}

}
