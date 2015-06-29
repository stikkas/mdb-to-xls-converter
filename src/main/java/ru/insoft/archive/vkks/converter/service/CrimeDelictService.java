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
 * для режима CRIME_AND_DELICT("1-1-7-СД-Преступность и правонарушения",
 * "%Преступность и правонарушения%")
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class CrimeDelictService extends TraceTomNumbersService {

	public CrimeDelictService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

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
			documents.add(new XLSDocument("б/н", delo.getEndDate(), "Брошюра со сведениями о "
					+ "преступности и судимости в РФ и её субъектах",
					countPages(pdfLink), pdfLink, "Преступность и правонарушения"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-7" + delo.getEndDate().get(Calendar.YEAR),
				delo.getTitle(), getTomNumber(delo), delo.getStartDate(), delo.getEndDate());
	}

}
