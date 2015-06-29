package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * для режима LAW_STAT("1-4-регл_судебная статистика", Arrays.asList(79552,
 * 79551)),
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class LawStatService extends Service {

	public LawStatService(Path workDir) {
		super(workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		String pdfLink = getPdfLink(delo.getGraph());
		if (createTitle(pdfLink)) {
			documents.add(new XLSDocument("б/н", delo.getEndDate(), delo.getTitle(),
					countPages(pdfLink), pdfLink, "Титульный лист"));
		}

		for (Document d : delo.getDocuments()) {
			Calendar date = d.getDate();
			if (date == null) {
				date = delo.getEndDate();
			}
			pdfLink = getPdfLink(d.getGraph());
			documents.add(new XLSDocument(d.getReportFormNumber(), date, d.getTitle(),
					countPages(pdfLink), d.getLawCourtName()
					+ d.getSubjectNameRF() + d.getReportPeriod()
					+ d.getReportType(), pdfLink, "Регламентная судебная статистика"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-4" + delo.getEndDate().get(Calendar.YEAR),
				delo.getTitle(), getTomNumber(delo.getTom()), delo.getStartDate(), delo.getEndDate());

	}

}
