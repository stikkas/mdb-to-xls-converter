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
 * для режима REVIEW_REPORT("1-1-1-обзоры_доклады", Arrays.asList(79558))
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class ReviewReportService extends Service {

	public ReviewReportService(Path workDir) {
		super(workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {

		List<XLSDocument> documents = new ArrayList<>();

		String pdfLink = getPdfLink(delo.getGraph());
		if (createTitle(pdfLink)) {
			Calendar cal = Calendar.getInstance();
			cal.set(1998, 11, 31);
			documents.add(new XLSDocument("б/н", cal, delo.getTitle(),
					countPages(pdfLink), pdfLink, "Титульный лист"));
		}

		for (Document doc : delo.getDocuments()) {
			Calendar cal = Calendar.getInstance();
			cal.set(1998, 11, 31);
			pdfLink = getPdfLink(doc.getGraph());
			documents.add(new XLSDocument("МЮ-б\\н", cal, doc.getTitle(), countPages(pdfLink),
					pdfLink, "обзоры_доклады"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		Calendar startDate = Calendar.getInstance();
		startDate.set(1998, 0, 1);
		Calendar endDate = Calendar.getInstance();
		endDate.set(1998, 11, 31);
		return new XLSDelo("1-1-1-1998", delo.getTitle(), getTomNumber(delo.getTom()),
				startDate, endDate);

	}

}
