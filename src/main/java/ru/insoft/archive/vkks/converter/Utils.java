package ru.insoft.archive.vkks.converter;

import java.util.Calendar;

/**
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class Utils {

	public static Calendar getCalendarFromYear(String year, boolean start) {
		Calendar cal = null;
		if (year == null || year.trim().isEmpty()) {
			return cal;
		}
		try {
			int yearNumber = Integer.valueOf(year);
			cal = Calendar.getInstance();
			if (start) {
				cal.set(yearNumber, 0, 1);
			} else {
				cal.set(yearNumber, 11, 31);
			}
		} catch (NumberFormatException ex) {
		}
		return cal;
	}
}
