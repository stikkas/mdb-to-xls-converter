package ru.insoft.archive.vkks.converter.tsts;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Благодатских С.
 */
public class Tests {

	public static void main(String[] args) {

		Path p = Paths.get(FilenameUtils.separatorsToSystem("6-3-03_2007-2007\\rc_5834810\\rc_5834810.xml"));
		System.out.println(p);
		System.out.println(p.toFile().getParent());
		System.out.println(p.toString().substring(0, p.toString().length() - 4));
		System.out.println(FilenameUtils.separatorsToWindows(p.toString()));
		p = Paths.get("/", "home", "basa", "file", "file.txt");
		Path parent = Paths.get("/", "home", "basa");
		System.out.println(p.relativize(parent));
		int end = p.getNameCount();
		System.out.println(p + " has " + end + " names");
		int start = parent.getNameCount();
		System.out.println(parent + " has " + start + " names");
		System.out.println(p.subpath(start, end));

		Stat stat = new Stat();
		stat.cases = 10;
		stat.docs = 400;
		stat.casesFound = 5;
		stat.casesNotFound = 5;
		stat.docsFound = 400;
		System.out.println(stat);
	}

	private static class Stat {

		/**
		 * Кол-во обработанных дел
		 */
		long cases;
		/**
		 * Кол-во обработанных документов
		 */
		long docs;
		/**
		 * количество отождествлённых дел
		 */
		long casesFound;
		/**
		 * количество отождествлённых документов
		 */
		long docsFound;
		/**
		 * количество неотождествлённых дел
		 */
		long casesNotFound;
		/**
		 * количество неотождествлённых документов
		 */
		long docsNotFound;

		private final String casesLabel = "Обработанных дел";
		private final String docsLabel = "Обработанных документов";
		private final String casesFoundLabel = "Отождествленных дел";
		private final String docsFoundLabel = "Отождествленных документов";
		private final String casesNotFoundLabel = "Неотождествленных дел";
		private final String docsNotFoundLabel = "Неотождествленных документов";
		@Override
		public String toString() {
			return String.format("---------------------------------------------\n"
					+ "| %30s:%10d |\n"
					+ "| %30s:%10d |\n"
					+ "| %30s:%10d |\n"
					+ "| %30s:%10d |\n"
					+ "| %30s:%10d |\n"
					+ "| %30s:%10d |\n"
					+ "---------------------------------------------\n",
					casesLabel, cases, docsLabel, docs, casesFoundLabel, 
					casesFound, docsFoundLabel, docsFound, casesNotFoundLabel,
					casesNotFound, docsNotFoundLabel, docsNotFound);
		}

	}
}
