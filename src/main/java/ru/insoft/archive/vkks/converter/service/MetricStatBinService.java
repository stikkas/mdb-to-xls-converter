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
 * для режима METRIC_STAT_BIN("1-1-5-сборник показателей статотчетности",
 * Arrays.asList(79622, 79533, 79538, 79540, 79548, 79614))
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class MetricStatBinService extends ByBarcodService {

	public MetricStatBinService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		String pdfLink = getPdfLink(delo.getGraph());
		if (createTitle(pdfLink)) {
			documents.add(new XLSDocument("б/н", delo.getEndDate(), delo.getTitle(),
					countPages(pdfLink), pdfLink, "Титульный лист"));
		}
		for (Document doc : delo.getDocuments()) {
			pdfLink = getPdfLink(doc.getGraph());
			documents.add(new XLSDocument("МЮ-б\\н", delo.getEndDate(), doc.getTitle(),
					countPages(pdfLink), doc.getLawCourtName()
					+ doc.getSubjectNameRF() + doc.getReportPeriod()
					+ doc.getReportType(), pdfLink, "судебная статистика"));
		}
		return documents;
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-5-" + delo.getEndDate().get(Calendar.YEAR),
				delo.getTitle(), getTomNumber(delo.getTom()), delo.getStartDate(), delo.getEndDate());
	}
}
