package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.List;
import javax.persistence.EntityManager;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;

/**
 * Выбирает дела из mdb по списку id
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public abstract class ByIdsService extends Service {

	public ByIdsService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<Delo> getDelos(EntityManager em) {
		return em.createQuery("SELECT d FROM Delo d WHERE d.id in :ids", Delo.class)
				.setParameter("ids", mode.getIds()).getResultList();

	}

}
