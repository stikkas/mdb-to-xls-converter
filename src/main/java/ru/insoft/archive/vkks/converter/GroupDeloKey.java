package ru.insoft.archive.vkks.converter;

import java.util.Objects;

/**
 * Ключ для группировки томов одного дела за один год
 *
 * @author stikkas<stikkas@yandex.ru>
 */
class GroupDeloKey {

	private final int year;
	private final String number;

	public GroupDeloKey(int year, String number) {
		this.year = year;
		this.number = number;
	}

	@Override
	public int hashCode() {
		int hash = 7;
		hash = 53 * hash + year;
		hash = 53 * hash + Objects.hashCode(number);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		final GroupDeloKey other = (GroupDeloKey) obj;
		return year == other.year && Objects.equals(number, other.number);
	}

	@Override
	public String toString() {
		return number + "_" + year;
	}

}
