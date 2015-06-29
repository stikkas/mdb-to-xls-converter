package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.List;
import javax.persistence.EntityManager;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;

/**
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public abstract class LikeTitleExludeBarcodesService extends Service {

	public LikeTitleExludeBarcodesService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<Delo> getDelos(EntityManager em) {
		return em.createQuery("SELECT d FROM Delo d WHERE d.title LIKE :title and d.barCode NOT IN :codes", Delo.class)
				.setParameter("title", mode.getSearchCriteria())
				.setParameter("codes", mode.getBarCodes()).getResultList();
	}
}
