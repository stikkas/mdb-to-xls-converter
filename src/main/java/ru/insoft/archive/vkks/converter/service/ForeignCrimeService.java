package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;

/**
 * для режимов:
 * <ul>
 * <li>CRIME_ZARUB("1-1-6 Проблемы преступности (по материалам зарубежной
 * печати)", "%Проблемы преступности в капиталистических странах (по материалам
 * зарубежной печати)%")</li>
 * <li>CRIME_INOSTR("1-1-6 Проблемы преступности (по материалам иностранной
 * печати)", "%Проблемы преступности в капиталистических странах (по материалам
 * иностранной печати)%")</li>
 * </ul>
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class ForeignCrimeService extends LikeTitleService {

	public ForeignCrimeService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<XLSDocument> getDocuments(Delo delo) {
		return delo.getDocuments().stream().map(d -> {
			Integer tom = delo.getTom();
			Calendar cal = Calendar.getInstance();
			cal.set(delo.getStartDate().get(Calendar.YEAR), tom - 1, 1);
			return new XLSDocument(tom.toString(), cal, "Ежемесячный информационный бюллетень",
					1, getPdfLink(delo.getGraph()) + ";" + getPdfLink(d.getGraph()),
					"Ежемесячный информационный бюллетень ВИНИТИ");
		}).collect(Collectors.toList());
	}

	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-6" + delo.getStartDate().get(Calendar.YEAR),
				delo.getTitle(), delo.getTom(), delo.getStartDate(),
				delo.getEndDate());
	}

}
