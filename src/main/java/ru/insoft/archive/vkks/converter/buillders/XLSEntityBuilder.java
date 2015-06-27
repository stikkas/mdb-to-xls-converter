package ru.insoft.archive.vkks.converter.buillders;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
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
	 * Хранит номера существующих томов для режимов CRIME_STATUS_RU,
	 * CRIME_AND_DELICT
	 */
	private final Map<DeloKey, Integer> tomNumbers = new HashMap<>();
	private final ConvertMode mode;

	public XLSEntityBuilder(ConvertMode mode) {
		this.mode = mode;
	}

	public XLSDocument createXLSDocument(Delo delo, Document doc) {
		return null;
	}
	/*
	 Индекс дела = «1-4" + «значение года из \Delo\Date_end»
	 Заголовок дела = \Delo\Delo_title
	 № тома = \Delo\Number_tom, если \Delo\Number_tom = is Null, то \Delo\Number_tom = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end

	 Индекс дела = «1-1-5» + «-значение года из \Delo\Date_end»
	 Заголовок дела = \Delo\Delo_title
	 № тома = \Delo\Number_tom, если \Delo\Number_tom = is Null, то \Delo\Number_tom = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end

	 Индекс дела = «1-1-1» + «-1998»
	 Заголовок дела = \Delo\Delo_title
	 № тома = \Delo\Number_tom, если \Delo\Number_tom = is Null, то \Delo\Number_tom = 1
	 Дата дела с = 01.01.1998
	 Дата дела по = 31.12.1998
	 ===============================================================
	 Индекс дела = «1-1-2" + «значение года из \Delo\Date_end»
	 Заголовок дела = \Delo\Delo_title
	 № тома = \Delo\Number_tom, если в XLS файле уже есть запись о томе дела 
	 с ПОЧТИ тождественным заголовком (Заголовок дела содержит текст «Состояние преступности в России») 
	 и тождественными годами (Дата дела с и Дата дела по), то № тома = номер, следующий по порядку
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end

	 Индекс дела = «1-1-7" + «значение года из \Delo\Date_end»
	 Заголовок дела = \Delo\Delo_title
	 № тома = \Delo\Number_tom, если в XLS файле уже есть запись о томе дела 
	 с ПОЧТИ тождественным заголовком (Заголовок дела содержит текст «Преступность и правонарушения») 
	 и тождественными годами (Дата дела с и Дата дела по), то № тома = номер, следующий по порядку
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end
	 ==============================================================
	 Индекс дела = «1-2-2" + «значение года из \Delo\Date_end»
	 Заголовок дела = \Delo\Delo_title
	 № тома = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end

	 Индекс дела = «1-2-2" + «Приказы»
	 Заголовок дела = «Приказы»
	 № тома = 1
	 Дата дела с = 01.01.1972
	 Дата дела по = 31.12.1992

	 Индекс дела = «1-3" + « Правовая информатика»
	 Заголовок дела = «Правовая информатика»
	 № тома = 1
	 Дата дела с = \Delo\Date_start
	 Дата дела по = \Delo\Date_end
	 */

	public XLSDelo createXLSDelo(Delo delo) {
		switch (mode) {
			case CRIME_ZARUB:
			case CRIME_INOSTR:
				return new XLSDelo("1-1-6" + delo.getStartDate().get(Calendar.YEAR),
						delo.getTitle(), delo.getTom(), delo.getStartDate(),
						delo.getEndDate());
			case LAW_STAT:
				return getDelo("1-4", delo);
			case METRIC_STAT_BIN:
				return getDelo("1-1-5-", delo);
			case REVIEW_REPORT:
				return getDelo("1-1-1-1998", delo, 1998, 1998, 0, 11, 1, 31);
			case CRIME_STATUS_RU:
				return getDelo(delo, "1-1-2");
			case CRIME_AND_DELICT:
				return getDelo(delo, "1-1-7");
			case INSTRUCTIONS:
				return getDelo("1-2-2" + delo.getEndDate().get(Calendar.YEAR),
						delo, 1, delo.getTitle());
			case ORDERS:
				return getDelo("1-2-2Приказы", "Приказы", 1, 1972, 1992, 0, 11, 1, 31);
			default:// PUBLICATIONS:
				return getDelo("1-3 Правовая информатика", delo, 1, "Правовая информатика");
		}
	}

	/**
	 * Создает дело для режимов LAW_STAT и METRIC_STAT_BIN
	 *
	 * @param startPartIndex начальная часть индекса дела
	 * @param delo данные из базы данных
	 * @return дело для отображения в xls
	 */
	private XLSDelo getDelo(String startPartIndex, Delo delo) {
		return new XLSDelo(startPartIndex + delo.getEndDate().get(Calendar.YEAR),
				delo.getTitle(), getTomNumber(delo.getTom()), delo.getStartDate(), delo.getEndDate());
	}

	/**
	 * Создает дело для режимов CRIME_STATUS_RU, CRIME_AND_DELICT
	 *
	 * @param delo данные из базы данных
	 * @param startPartIndex начальная часть индекса дела
	 * @return дело для отображения в xls
	 */
	private XLSDelo getDelo(Delo delo, String startPartIndex) {
		return new XLSDelo(startPartIndex + delo.getEndDate().get(Calendar.YEAR),
				delo.getTitle(), getTomNumber(delo), delo.getStartDate(), delo.getEndDate());
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
	private int getTomNumber(Delo delo) {
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

	/**
	 * Создает дело для режимов REVIEW_REPORT
	 *
	 * @param index индекс дела
	 * @param delo дело из mdb
	 * @param startYear года даты с
	 * @param endYear год даты по
	 * @param startMonth месяц даты с (начинается с 0)
	 * @param endMonth месяц даты по (начинается с 0)
	 * @param startDay день даты с
	 * @param endDay день даты по
	 * @return дело для отображения в xls
	 */
	private XLSDelo getDelo(String index, Delo delo, int startYear, int endYear,
			int startMonth, int endMonth, int startDay, int endDay) {
		Calendar startDate = Calendar.getInstance();
		startDate.set(startYear, startMonth, startDay);
		Calendar endDate = Calendar.getInstance();
		endDate.set(endYear, endMonth, endDay);
		return new XLSDelo(index, delo.getTitle(), getTomNumber(delo.getTom()),
				startDate, endDate);
	}

	/**
	 * Создает дело для режимов ORDERS
	 *
	 * @param index индекс дела
	 * @param title заголовок дела
	 * @param tom номер тома дела
	 * @param startYear года даты с
	 * @param endYear год даты по
	 * @param startMonth месяц даты с (начинается с 0)
	 * @param endMonth месяц даты по (начинается с 0)
	 * @param startDay день даты с
	 * @param endDay день даты по
	 * @return дело для отображения в xls
	 */
	private XLSDelo getDelo(String index, String title, int tom, int startYear, int endYear,
			int startMonth, int endMonth, int startDay, int endDay) {
		Calendar startDate = Calendar.getInstance();
		startDate.set(startYear, startMonth, startDay);
		Calendar endDate = Calendar.getInstance();
		endDate.set(endYear, endMonth, endDay);
		return new XLSDelo(index, title, tom, startDate, endDate);
	}

	/**
	 * Создает дело для режимов PUBLICATIONS
	 *
	 * @param index индекс дела
	 * @param delo дело из mdb
	 * @param tom номер тома дела
	 * @param title заголовок дела
	 * @return дело для отображения в xls
	 */
	private XLSDelo getDelo(String index, Delo delo, int tom, String title) {
		return new XLSDelo(index, title, tom, delo.getStartDate(), delo.getEndDate());
	}

	/**
	 * Обрабатывает null для номера тома
	 *
	 * @param tom
	 * @return
	 */
	private int getTomNumber(Integer tom) {
		if (tom == null) {
			tom = 1;
		}
		return tom;
	}
}
