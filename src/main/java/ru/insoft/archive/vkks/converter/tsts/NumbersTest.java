package ru.insoft.archive.vkks.converter.tsts;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author stikkas<stikkas@yandex.ru>
 */
public class NumbersTest {
	private static Map<Integer, Integer> numbers = new HashMap<>();

	public static void main(String[] args) {
		numbers.put(10, 2);
		Integer i = numbers.get(10);
		++i;
		++i;
		++i;
		System.out.println(numbers.get(10));
	}
}
