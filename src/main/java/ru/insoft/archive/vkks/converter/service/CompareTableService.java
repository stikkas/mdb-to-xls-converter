package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * для режима COMPARE_TABLE("1-2-2-Сравнительная таблица статей УК",
 * Arrays.asList(79515))
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class CompareTableService extends ByBarcodService {

	public CompareTableService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	/**
	 * Формируем сведения о документе дела:
	 */
	/*
	 № регистрации = 1
	 Дата регистрации = \Delo\Date_end
	 Краткое содержание = \Delo \Delo_title
	 Количество листов = считать количество страниц в PDF-файле из \Delo\Graph_delo
	 Файлы = \Delo\Graph_delo
	 Наименование вида = «Материалы»
	 */
	@Override
	public List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile {
		List<XLSDocument> documents = new ArrayList<>();
		String link = getPdfLink(delo.getGraph());
		documents.add(new XLSDocument("1", delo.getEndDate(), delo.getTitle(),
				countPages(link), link, "Материалы"));
		return documents;
	}

	/**
	 * Формируем сведения о томе дела
	 */
	/*
	 Индекс дела = «1-1-2»
	 Заголовок дела = \Delo \Delo_title
	 № тома = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end
	 */
	@Override
	public XLSDelo getDelo(Delo delo) {
		return new XLSDelo("1-1-2", delo.getTitle(), 1, delo.getStartDate(),
				delo.getEndDate());
	}

}
