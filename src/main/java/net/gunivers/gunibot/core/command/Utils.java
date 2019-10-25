package net.gunivers.gunibot.core.command;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Utils {

    public static String getResourceFileContent(String p, String file) {
	try {
	    return String.join("\n",
		    Files.readAllLines(Paths.get(Utils.class.getClassLoader().getResource(p + File.separatorChar + file).toURI())));
	} catch (Exception e) {
	    e.printStackTrace();
	    return null;
	}
    }
}
