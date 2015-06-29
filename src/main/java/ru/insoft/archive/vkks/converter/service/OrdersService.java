package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * для режима ORDERS("1-2-2-Приказы", "%Приказ%"),
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class OrdersService extends LikeTitleService {

	public OrdersService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		String pdfLink = getPdfLink(delo.getGraph());
		if (createTitle(pdfLink)) {
			final String[] graph = new String[]{pdfLink};
			delo.getDocuments().forEach(doc -> {
				String link = getPdfLink(doc.getGraph());
				if (createTitle(link)) {
					graph[0] += ";" + link;
				}
			});
			documents.add(new XLSDocument(delo.getTom().toString(), delo.getStartDate(),
					delo.getTitle(), countPages(pdfLink), graph[0], "Приказ и приложения"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		Calendar startDate = Calendar.getInstance();
		startDate.set(1972, 0, 1);
		Calendar endDate = Calendar.getInstance();
		endDate.set(1992, 11, 31);
		return new XLSDelo("1-2-2Приказы", "Приказы", 1, startDate, endDate);
	}

}
