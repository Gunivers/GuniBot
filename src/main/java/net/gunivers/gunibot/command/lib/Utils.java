package net.gunivers.gunibot.command.lib;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Utils {

	public static String getResourceFileContent(String p, String file) {
		try {
			URI uri = ClassLoader.getSystemResource(p).toURI();
			String mainPath = Paths.get(uri).toString();
			Path path = Paths.get(mainPath, file);
			return Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
			} catch(Exception e) {
				e.printStackTrace();
				return null;
			}
	}

}
