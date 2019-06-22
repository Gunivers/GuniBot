package net.gunivers.gunibot.core.main_parser;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class ArgumentParser {

	private HashMap<String,String> arguments;

	public ArgumentParser(String... args) {
		arguments = parseArguments(args);
	}

	public static HashMap<String,String> parseArguments(String... args) {
		HashMap<String,String> output = new HashMap<>(args.length);
		StringAnalyzer analyzer = new StringAnalyzer(args);

		String current_key = "";
		while (analyzer.hasNext()) {
			String str = analyzer.next();

			if(current_key.isEmpty()) {
				if (str.startsWith("-")) {
					current_key = str.substring(1);
				} else {
					throw new IllegalArgumentException("La valeur '"+str+"' n'est assigné à aucune option : "+String.join(" ", args));
				}
			} else {
				if (str.startsWith("-")) {
					output.put(current_key, "");
					current_key = str.substring(1);
				} else {
					output.put(current_key, str);
					current_key = "";
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
