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
	}
}
