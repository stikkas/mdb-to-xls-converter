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
 * для режима INSTRUCTIONS("1-2-2-Инструкции", "%Инструкция%")
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class InstructionsService extends LikeTitleService {

	public InstructionsService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
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
			documents.add(new XLSDocument("б/н", doc.getDate(), doc.getTitle(),
					countPages(pdfLink), graph, "Инструкция"));
		}
		return documents;

	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-2-2" + delo.getEndDate().get(Calendar.YEAR),
				delo.getTitle(), 1, delo.getStartDate(), delo.getEndDate());

	}

}
