package ru.insoft.archive.vkks.converter.buillders;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import ru.insoft.archive.vkks.converter.ConvertMode;
import ru.insoft.archive.vkks.converter.domain.Delo;
import ru.insoft.archive.vkks.converter.error.WrongModeException;

/**
 * Создает SQL запрос для определенного режима работы
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class QueryBuilder {

	private final EntityManager em;
	private final ConvertMode mode;

	public QueryBuilder(EntityManager em, ConvertMode mode) {
		this.em = em;
		this.mode = mode;
	}

	public Query createQuery() throws WrongModeException {
		Query query = null;
		switch (mode) {
			case CRIME_ZARUB:
			case CRIME_INOSTR:
			case CRIME_STATUS_RU:
			case CRIME_AND_DELICT:
			case INSTRUCTIONS:
			case ORDERS:
				query = em.createQuery("SELECT d FROM Delo d WHERE d.title LIKE :title", Delo.class)
						.setParameter("title", mode.getSearchCriteria());
				break;
			case LAW_STAT:
			case METRIC_STAT_BIN:
			case REVIEW_REPORT:
			case PUBLICATIONS:
				query = em.createQuery("SELECT d FROM Delo d WHERE d.barCode in :codes", Delo.class)
						.setParameter("codes", mode.getBarCodes());
				break;
			default:
				throw new WrongModeException("Неизвестный режим работы: " + mode);
		}
		return query;
	}

}
