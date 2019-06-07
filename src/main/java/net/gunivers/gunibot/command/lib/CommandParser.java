package net.gunivers.gunibot.command.lib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.keys.KeyEnum;
import net.gunivers.gunibot.command.lib.keys.KeyEnum.Position;
import net.gunivers.gunibot.command.lib.nodes.Node;
import net.gunivers.gunibot.command.lib.nodes.NodeList;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public class CommandParser {
	
	private static Command command;

	/**
	 * Parse la syntaxe d'une commande
	 * 
	 * @param c
	 *            la commande dont la syntaxe doit être parsée
	 * @return la liste des alias de la commande
	 */
	public static NodeList<String> parseCommand(Command c) {
		try {
			command = c;
			String s = Utils.getResourceFileContent("commands/", c.getSyntaxFile());
			JSONObject obj = new JSONObject(s);
			NodeList<String> n = parseRoot(obj);
			c.setSyntax(n);
			command = null;
			return n;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static NodeList<String> parseRoot(JSONObject obj) throws JSONException, Exception {
		List<String> keys = obj.keySet().stream().filter(x -> !x.equals("comment")).collect(Collectors.toList());
		if (keys.size() == 1) {
			String keyRoot = keys.get(0);
			return parseHead(obj.getJSONObject(keyRoot), keyRoot);
		}
		throw new JsonCommandFormatException("Racine invalide\n\tat " + command.getSyntaxFile());
	}

	@SuppressWarnings("unchecked")
	private static NodeList<String> parseHead(JSONObject obj, String root) throws Exception {
		if (obj.keySet().stream().distinct().count() == obj.keySet().size()) {
			NodeList<String> nr = new NodeList<>(root, (s, l) -> l.contains(s));
			return (NodeList<String>) parseArgument(obj, nr, Position.ONLY_IN_ROOT, Position.DEFAULT);
		}
		throw new JsonCommandFormatException("En-tête de fichier invalide\n\tat " + command.getSyntaxFile());
	}

	public static List<Node> parseArguments(JSONArray array) {
		List<Node> list = new LinkedList<>();
		for (int i = 0; i < array.length(); i++)
			list.add(parseArgument(array.getJSONObject(i), null, Position.NOT_IN_ROOT, Position.DEFAULT));
		return list;
	}

	private static Node parseArgument(JSONObject obj, Node n, Position pos, Position pos2) {

		try {
			int nbrKeys = (int) obj.keySet().stream().filter(s -> !s.equals("comment")).count();
			if (nbrKeys != obj.keySet().stream().filter(s -> !s.equals("comment")).distinct().count())
				throw new JsonCommandFormatException("Multiplicité d'une clé dans un argument\n\tat " + command.getSyntaxFile());
			
			Set<String> keysPresent = new HashSet<>(Arrays.asList("comment")); 
			for(KeyEnum ke : KeyEnum.getByPos(pos, pos2)) {
				Tuple2<String, Node> result = ke.getClazz().parseJson(obj, n, command);
				keysPresent.add(result._1);
				n = result._2;
			}
			String invalidKeys = obj.keySet().stream().filter(s -> !keysPresent.contains(s)).collect(Collectors.joining(", "));
			if(!invalidKeys.equals(""))
				throw new JsonCommandFormatException("Clé(s) invalide(s) " + invalidKeys + "\n\tat " + command.getSyntaxFile());

			return n;

		} catch (JsonCommandFormatException e) {
			e.printStackTrace();
			return null;
		}
	}

}
