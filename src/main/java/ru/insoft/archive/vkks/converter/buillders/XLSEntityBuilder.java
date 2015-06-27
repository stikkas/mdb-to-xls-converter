package ru.insoft.archive.vkks.converter.buillders;

import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.domain.Document;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;

/**
 * Создает XLSDelo или XLSDocument для определенного режима работы конвертора
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class XLSEntityBuilder {

	private final ConvertMode mode;

	public XLSEntityBuilder(ConvertMode mode) {
		this.mode = mode;
	}

	public XLSDocument createXLSDocument(Delo delo, Document doc) {
		return null;
	}

	public XLSDelo createXLSDelo(Delo delo) {
		return null;
	}
}
