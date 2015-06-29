package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.List;
import javax.persistence.EntityManager;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;

/**
 * Выбирает дела с определенными Case_barcod из mdb
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public abstract class ByBarcodService extends Service {

	public ByBarcodService(ConvertMode mode, Path workDir) {
		super(mode, workDir);
	}

	@Override
	public List<Delo> getDelos(EntityManager em) {
		return em.createQuery("SELECT d FROM Delo d WHERE d.barCode in :codes", Delo.class)
				.setParameter("codes", mode.getBarCodes()).getResultList();
	}

}
