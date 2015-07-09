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
 * для режима MAIN_CRITERIAS("1-1-3-Сборник основных показателей", "%Сборник
 * основных показателей%"),
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class MainCriteriaService extends LikeTitleService {

	public MainCriteriaService(ConvertMode mode, Path workDir) {
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
					+ "_" + doc.getReportPeriod(), pdfLink,"Статистический сборник"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {

		return new XLSDelo("1-1-3-" + delo.getTitleYearOfCompletion(),
				delo.getTitle(), 1, getCalendarFromYear(delo.getTitleYearOfCreation(), true),
				getCalendarFromYear(delo.getTitleYearOfCompletion(), false));
	}



}
