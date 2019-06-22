package net.gunivers.gunibot.core.main_parser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.NoSuchElementException;

public class ConfigParser {

	private HashMap<String,String> arguments;

	public ConfigParser(File file) throws IOException {
		arguments = parseArguments(file);
	}

	public static HashMap<String,String> parseArguments(File file) throws IOException {
		FileManip file_reader = new FileManip(file).supprimeLignesVides();
		HashMap<String,String> output = new HashMap<>(file_reader.size());


		for (String ligne:file_reader) {
			if(ligne.startsWith("#")) continue;
			else {
				String[] pair = ligne.split("=");
				if(pair.length <= 1) throw new IllegalArgumentException("La ligne suivante n'est pas une paire 'clef=value' : "+ligne);
				else if(pair.length > 2) throw new IllegalArgumentException("Impossible de parsé plusieurs signes '=' dans une même ligne : "+ligne);
				else {
					if(pair[0].isEmpty()) {
						throw new IllegalArgumentException("Impossible de parsé une clef vide : "+ligne);
					} else {
						output.put(pair[0], pair[1]);
					}
				}
			}

		}
		return output;
	}

	public boolean hasArguments(String key) {
		return arguments.containsKey(key);
	}

	public String getArguments(String key) {
		if (arguments.containsKey(key)) {
			return arguments.get(key);
		} else {
			throw new NoSuchElementException("Aucun argument '"+key+"' n'a été entré");
		}
	}

	public String getDefaultArguments(String key, String default_arg) {
		return arguments.getOrDefault(key, default_arg);
	}

}
