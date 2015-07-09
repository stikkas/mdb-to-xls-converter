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
 * для режима CRIME_AND_DELICT2("1-1-7_Преступность и правонарушения",
 * "%Преступность и правонарушения%")
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class CrimeDelict2Service extends LikeTitleService {

	public CrimeDelict2Service(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();

		for (Document doc : delo.getDocuments()) {
			String pdfLink = getPdfLink(delo.getGraph());
			documents.add(new XLSDocument("б/н", doc.getDate(),
					"Статистический сборник со сведениями о преступности и судимости "
					+ "в России, ее регионах и других государствах СНГ",
					countPages(pdfLink), pdfLink, "Статистический сборник"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-2" + delo.getEndDate().get(Calendar.YEAR),
				delo.getTitle(), 1, delo.getStartDate(), delo.getEndDate());
	}
}
