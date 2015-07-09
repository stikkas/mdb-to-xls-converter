package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import ru.insoft.archive.vkks.converter.ConvertMode;
import static ru.insoft.archive.vkks.converter.Utils.getCalendarFromYear;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * для режима ABOUT_FACES("1-1-2-о лицах", "%о лицах%")
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class AboutFacesService extends LikeTitleService {

	public AboutFacesService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();

		Calendar regDate = getCalendarFromYear(delo.getTitleYearOfCompletion(), false);
		for (Document doc : delo.getDocuments()) {
			String pdfLink = getPdfLink(doc.getGraph());
			documents.add(new XLSDocument(doc.getReportFormNumber(), regDate, doc.getTitle(),
					countPages(pdfLink), doc.getDataForm() + "_"
					+ doc.getSubjectNameRF() + "_" + doc.getLawCourtName()
					+ "_" + doc.getReportPeriod(), pdfLink, "Статистический сборник"));
		}
		return documents;

	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-2-" + delo.getTitleYearOfCompletion(),
				delo.getTitle(), 1, getCalendarFromYear(delo.getTitleYearOfCreation(), true),
				getCalendarFromYear(delo.getTitleYearOfCompletion(), false));
	}

}
