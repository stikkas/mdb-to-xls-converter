package ru.insoft.archive.vkks.converter.service;

import java.nio.file.Path;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import ru.insoft.archive.vkks.converter.domain.Delo;

/**
 * Промежуточный класс для CRIME_STATUS_RU, CRIME_AND_DELICT
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public abstract class TraceTomNumbersService extends Service {

	private final Map<DeloKey, Integer> tomNumbers = new HashMap<>();

	private class DeloKey {

		private final int startYear;
		private final int endYear;

		public DeloKey(Calendar start, Calendar end) {
			startYear = start.get(Calendar.YEAR);
			endYear = end.get(Calendar.YEAR);
		}

		@Override
		public boolean equals(Object other) {
			if (other != null && other instanceof DeloKey) {
				DeloKey another = (DeloKey) other;
				return startYear == another.startYear && endYear == another.endYear;
			}
			return false;
		}

		@Override
		public int hashCode() {
			int hash = 7;
			hash = 17 * hash + this.startYear;
			hash = 17 * hash + this.endYear;
			return hash;
		}

	}

	/**
	 * Возвращает номер тома для дела по следующему алгоритму: № тома =
	 * \Delo\Number_tom, если в XLS файле уже есть запись о томе дела
	 * тождественными годами (Дата дела с и Дата дела по), то № тома = номер,
	 * следующий по порядку, если таких дел нет и \Delo\Number_tom is null - 1
	 *
	 * @param delo данные из базы данных
	 * @return номер тома
	 */
	protected int getTomNumber(Delo delo) {
		DeloKey key = new DeloKey(delo.getStartDate(), delo.getEndDate());
		Integer number = tomNumbers.get(key);
		if (number != null) {
			++number;
		} else {
			number = getTomNumber(delo.getTom());
		}
		tomNumbers.put(key, number);
		return number;
	}

	public TraceTomNumbersService(Path workDir) {
		super(workDir);
	}

}
