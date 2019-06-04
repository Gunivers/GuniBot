package net.gunivers.gunibot.command.lib;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.nodes.Node;
import net.gunivers.gunibot.command.lib.nodes.NodeList;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public class FunctionsParser {

	public static void parseFunction(Function f) {
			Set<String> files = f.getFunctionFiles();		
			while(files.iterator().hasNext()) {
				String s = Utils.getResourceFileContent("functions/", files.iterator().next());
				JSONObject obj = new JSONObject(s);
				Tuple2<String, Node> n;
				try {
					n = parseRoot(obj, f);
//					Function.functions.put(n._1, Tuple.newTuple(n._2, f));
				} catch (JsonCommandFormatException e) {
					e.printStackTrace();
				}
			}
	}
	
	private static Tuple2<String, Node> parseRoot(JSONObject obj, Function f) throws JsonCommandFormatException {
		List<String> keys = obj.keySet().stream().filter(x -> !x.equals("comment")).collect(Collectors.toList());
		if (keys.size() == 1) {
			String keyRoot = keys.get(0);
			return Tuple.newTuple(keyRoot, parseHead(obj.getJSONObject(keyRoot), keyRoot));
		}
		throw new JsonCommandFormatException("Racine invalide");
	}
	
	private static Node parseHead(JSONObject obj, String root) throws JsonCommandFormatException {
		if (obj.keySet().stream().distinct().count() == obj.keySet().size()) {
			NodeList<String> nr = new NodeList<>(root, (s, l) -> l.contains(s));
			for (String key : obj.keySet()) {
				switch (key) {
				case "arguments":
					nr.setChild(parseArguments(obj.getJSONArray(key)));
					break;
				case "comment":
					break;
				default:
					throw new JsonCommandFormatException("Clé d'en-tête \"" + key + "\" invalide");
				}
			}
		}
		throw new JsonCommandFormatException("En-tête de fichier invalide");
	}
	
	private static List<Node> parseArguments(JSONArray array) {
		List<Node> list = new LinkedList<>();
		for(int i = 0; i < array.length(); i++)
			list.add(parseArgument(array.getJSONObject(i)));
		return list;
	}
	
	private static Node parseArgument(JSONObject obj) {
		return null;
	}

}
