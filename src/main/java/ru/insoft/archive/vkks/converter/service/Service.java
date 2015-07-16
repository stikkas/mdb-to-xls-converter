package ru.insoft.archive.vkks.converter.service;

import com.itextpdf.text.pdf.PdfReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import org.apache.commons.io.FilenameUtils;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.dto.XLSDelo;
import ru.insoft.archive.vkks.converter.dto.XLSDocument;
import ru.insoft.archive.vkks.converter.error.WrongPdfFile;

/**
 * Сервис для создания различных сервисов, которые формируют необходимые XLSDelo
 * и XLSDocument в зависимости от режима работы.
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public abstract class Service {

	protected final Path workDir;
	/**
	 * Преобразует ссылку из базы данных в относительный путь к исходному файлу.
	 * В базе данных ссылка представлена в Windows формате. Удаляются лишние
	 * символы на начале и конце.
	 *
	 * @param link ссылка на файл данных
	 * @return путь к файлу в Windows формате
	 */
	protected String getPdfLink(String link) {
		if (link != null) {
			link = link.trim();
			if (link.startsWith("#")) {
				link = link.substring(1);
			}
			if (link.endsWith("#")) {
				link = link.substring(0, link.length() - 1);
			}
			if (link.contains("#")) { // Бывают и такие кривые ссылки
				link = link.split("#")[0];
			}
		}
		return link;
	}

	/**
	 * Определяет создавать или нет титульный лист
	 *
	 * @param link
	 * @return
	 */
	protected boolean createTitle(String link) {
		return (link != null && !link.trim().isEmpty());
	}

	/**
	 * Подситываем кол-во страниц документов на входе получает относительный
	 * путь к файлу в формате Windows
	 *
	 * @param pdfFileName строка с относительным путем до pdf файла
	 * @return кол-во листов заданного файла
	 * @throws ru.insoft.archive.vkks.converter.error.WrongPdfFile в случае если
	 * невозможно прочитать файл как pdf
	 */
	protected int countPages(String pdfFileName) throws WrongPdfFile {
		try {
			pdfFileName = FilenameUtils.separatorsToSystem(pdfFileName);
			PdfReader reader = new PdfReader(workDir.resolve(pdfFileName).toString());
			int pages = reader.getNumberOfPages();
			reader.close();
			return pages;
		} catch (IOException | NullPointerException ex) {
			throw new WrongPdfFile("Невозможно получить кол-во страниц " + pdfFileName + ": " + ex.getMessage());
		}
	}

	/**
	 * Обрабатывает null для номера тома
	 *
	 * @param tom
	 * @return
	 */
	protected int getTomNumber(Integer tom) {
		if (tom == null) {
			tom = 1;
		}
		return tom;
	}

	public Service(Path workDir) {
		this.workDir = workDir;
	}

	public static Optional<Service> getInstance(Path workDir) {
		Optional<Service> object;
		try {
			Constructor<? extends Service> ctor = CommonMinustService.class.getConstructor(Path.class);
			object = Optional.of(ctor.newInstance(workDir));
		} catch (NullPointerException | NoSuchMethodException | SecurityException | InstantiationException |
				IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
			object = Optional.empty();
		}
		return object;
	}

	/**
	 * Создает список XLSDocument записей для помещения на лист Документы для
	 * заданного дела
	 *
	 * @param delo запись из таблицы Delo mdb
	 * @return список для записи в XLS файл
	 * @throws ru.insoft.archive.vkks.converter.error.WrongPdfFile
	 */
	public abstract List<XLSDocument> getDocuments(Delo delo) throws WrongPdfFile;

	/**
	 * Создает XLSDelo запись для помещения на лист Дело для заданного дела
	 *
	 * @param delo запись из таблицы Delo mdb
	 * @return запись для XLS файла
	 */
	public abstract XLSDelo getDelo(Delo delo);

	/**
	 * Получает список дел из mdb файла
	 *
	 * @param em EntityManager для выбранного mdb файла
	 * @return список дел
	 */
	public abstract List<Delo> getDelos(EntityManager em);
}
