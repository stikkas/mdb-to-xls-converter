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
 * для режима RABOTA_NAROD_LAW(Arrays.asList(484), "1-4-1-Работа народных
 * судов");
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class RabotaNarodLawService extends ByIdsService {

	public RabotaNarodLawService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();

		Calendar cal = Calendar.getInstance();
		cal.set(1998, 11, 31);
		String pdfLink = getPdfLink(delo.getGraph());
		try {
			if (createTitle(pdfLink)) {
				documents.add(new XLSDocument("б/н", cal, "Титульный лист",
						countPages(pdfLink), pdfLink, "Титульный лист"));
			}
		} catch (WrongPdfFile ex) {
			System.out.println(ex.getMessage());
		}
		for (Document doc : delo.getDocuments()) {
			pdfLink = getPdfLink(doc.getGraph());
			documents.add(new XLSDocument(doc.getReportFormNumber(), cal, doc.getTitle(),
					countPages(pdfLink), doc.getDataForm() + "_"
					+ doc.getSubjectNameRF() + "_" + doc.getLawCourtName()
					+ "_" + doc.getReportPeriod(), pdfLink, doc.getDocNumber()));
		}
		return documents;

	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		Calendar start = Calendar.getInstance();
		start.set(1998, 0, 1);
		Calendar end = Calendar.getInstance();
		end.set(1998, 11, 31);
		return new XLSDelo("1-4-1-", delo.getTitle(), 1, start, end);
	}

}
