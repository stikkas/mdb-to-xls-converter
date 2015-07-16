package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import static java.util.stream.Collectors.toList;
import javax.persistence.EntityManager;
import static ru.insoft.archive.vkks.converter.Utils.getCalendarFromYear;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class CommonMinustService extends Service {

	public CommonMinustService(Path workDir) {
		super(workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();

		final String pdfLink = getPdfLink(delo.getGraph());

		boolean contains = delo.getDocuments().stream().anyMatch(d -> {
			return getPdfLink(d.getGraph()).equals(pdfLink);
		});

		if (createTitle(pdfLink) && !contains) {
			Calendar regDate = getCalendarFromYear(delo.getTitleYearOfCompletion(), false);
			if (regDate == null) {
				regDate = Calendar.getInstance();
				regDate.set(2015, 6, 30);
			}

			documents.add(new XLSDocument("б/н", regDate, "Обложка дела \\ Титульный лист",
					countPages(pdfLink), pdfLink, "Обложка дела \\ Титульный лист"));
		}

		for (Document doc : delo.getDocuments()) {
			String pdfLink1 = getPdfLink(doc.getGraph());
			Calendar docRegDate = doc.getDate();
			if (docRegDate == null) {
				docRegDate = getCalendarFromYear(doc.getReportYear(), false);
				if (docRegDate == null) {
					docRegDate = Calendar.getInstance();
					docRegDate.set(2015, 6, 30);
				}
			}
			String title = doc.getTitle();
			if (title == null || title.trim().isEmpty()) {
				title = doc.getDocNumber();
			}
			documents.add(new XLSDocument(doc.getReportFormNumber(), docRegDate, title,
					countPages(pdfLink1),
					String.join("_",
							Arrays.asList(doc.getReportYear(), doc.getReportPeriod(),
									doc.getReportType(), doc.getSubjectNameRF(), doc.getLawCourtName(), doc.getDataForm())
							.stream()
							.map(s -> {
								if (s == null || s.trim().isEmpty()) {
									return " ";
								}
								return s;
							}).collect(toList())), pdfLink1, doc.getCategory()));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		Calendar start = getCalendarFromYear(delo.getTitleYearOfCreation(), true);
		if (start == null) {
			start = Calendar.getInstance();
			start.set(2015, 6, 30);
		}
		Calendar end = getCalendarFromYear(delo.getTitleYearOfCompletion(), false);
		if (end == null) {
			end = Calendar.getInstance();
			end.set(2015, 6, 30);
		}

		
		return new XLSDelo("-" + (delo.getEndDate() == null ? "" : delo.getEndDate().get(Calendar.YEAR)),
				delo.getTitle(), 1, start, end);
	}

	@Override
	public List<Delo> getDelos(EntityManager em) {
		return em.createQuery("SELECT d FROM Delo d", Delo.class)
				.getResultList();
	}

}
