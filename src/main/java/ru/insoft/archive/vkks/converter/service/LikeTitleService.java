package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.List;
import javax.persistence.EntityManager;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;

/**
 * Выбирает дела из mdb по схожести с заголоком дела
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public abstract class LikeTitleService extends Service {

	public LikeTitleService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<Delo> getDelos(EntityManager em) {
		return em.createQuery("SELECT d FROM Delo d WHERE d.title LIKE :title", Delo.class)
				.setParameter("title", mode.getSearchCriteria()).getResultList();

	}

}
