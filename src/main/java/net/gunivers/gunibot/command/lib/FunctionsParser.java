//package net.gunivers.gunibot.command.lib;
//
//import java.nio.file.Path;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//import org.json.JSONObject;
//
//import net.gunivers.gunibot.utils.tuple.Tuple2;
//
//public class FunctionsParser {
//
//	public static void parseFunction(Function f) {
//			Set<String> files = f.getFunctionFiles();		
//			while(files.iterator().hasNext()) {
//				String s = Utils.getResourceFileContent("functions/", files.iterator().next());
//				JSONObject obj = new JSONObject(s);
//				Tuple2<String, Node> n = parseRoot(obj, f);
////				Function.functions.put(n._1, f);
//			}
//	}
//	
//	private static Tuple2<String, Node> parseRoot(JSONObject obj, Function f) throws JsonCommandFormatException {
//		List<String> keys = obj.keySet().stream().filter(x -> !x.equals("comment")).collect(Collectors.toList());
//		if (keys.size() == 1) {
//			String keyRoot = keys.get(0);
//			return parseHead(obj.getJSONObject(keyRoot), keyRoot);
//		}
//		throw new JsonCommandFormatException("Racine invalide");
//	}
//	
//	private static Node parseHead(JSONObject obj, String root) {
//		if (obj.keySet().stream().distinct().count() == obj.keySet().size()) {
//			NodeRoot nr = new NodeRoot(root);
//			for (String key : obj.keySet()) {
//				switch (key) {
//				case "arguments":
//					nr.setChild(parseArguments(obj.getJSONArray(key), c));
//					break;
//				case "comment":
//					break;
//				default:
//					throw new JsonCommandFormatException("Clé d'en-tête \"" + key + "\" invalide");
//				}
//			}
//			if (c.getDescription() != "")
//				return nr;
//		}
//		throw new JsonCommandFormatException("En-tête de fichier invalide");
//	}
//
//}
