package net.gunivers.gunibot.command.lib;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.nodes.Node;
import net.gunivers.gunibot.command.lib.nodes.NodeEnum;
import net.gunivers.gunibot.command.lib.nodes.NodeList;
import net.gunivers.gunibot.command.lib.nodes.TypeNode;

public class CommandParser {

	/**
	 * Parse la syntaxe d'une commande
	 * 
	 * @param c
	 *            la commande dont la syntaxe doit être parsée
	 * @return la liste des alias de la commande
	 */
	public static NodeList<String> parseCommand(Command c) {
		try {
			String s = Utils.getResourceFileContent("commands/", c.getSyntaxFile());
			JSONObject obj = new JSONObject(s);
			NodeList<String> n = parseRoot(obj, c);
			c.setSyntax(n);
			return n;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static NodeList<String> parseRoot(JSONObject obj, Command c) throws JSONException, Exception {
		List<String> keys = obj.keySet().stream().filter(x -> !x.equals("comment")).collect(Collectors.toList());
		if (keys.size() == 1) {
			String keyRoot = keys.get(0);
			return parseHead(obj.getJSONObject(keyRoot), keyRoot, c);
		}
		throw new JsonCommandFormatException("Racine invalide\n\tat " + c.getSyntaxFile());
	}

	private static NodeList<String> parseHead(JSONObject obj, String root, Command c) throws Exception {
		if (obj.keySet().stream().distinct().count() == obj.keySet().size()) {
			NodeList<String> nr = new NodeList<>(root, (s, l) -> l.contains(s));
			for (String key : obj.keySet()) {
				switch (key) {
				case "description":
					c.setDescription(obj.getString(key));
					break;
				case "permissions":
					c.addPermissions(obj.getJSONArray(key).toList().stream().map(s -> s.toString())
							.collect(Collectors.toList()));
					break;
				case "aliases":
					nr.addElements(obj.getJSONArray(key).toList().stream().map(s -> s.toString())
							.collect(Collectors.toList()));
					break;
				case "arguments":
					nr.setChild(parseArguments(obj.getJSONArray(key), c));
					break;
				case "execute":
					Method m = getMethodByName(obj.getString(key), c);
					nr.setExecute(m);
					if (m == null)
						throw new JsonCommandFormatException("La fonction " + obj.getString(key)
								+ " n'existe pas dans la classe " + c.getClass().getSimpleName());
					break;
				case "comment":
					break;
				default:
					throw new JsonCommandFormatException("Clé d'en-tête \"" + key + "\" invalide\n\\at " + c.getSyntaxFile());
				}
			}
			if (c.getDescription() != "")
				return nr;
		}
		throw new JsonCommandFormatException("En-tête de fichier invalide\n\tat " + c.getSyntaxFile());
	}

	protected static List<Node> parseArguments(JSONArray array, Command c) {
		List<Node> list = new LinkedList<>();
		for (int i = 0; i < array.length(); i++)
			list.add(parseArgument(array.getJSONObject(i), c));
		return list;
	}

	private static Node parseArgument(JSONObject obj, Command c) {
		Node n = null;
		String type = "";
		String matches = "";
		String tag = "";
		boolean keepValue = false;
		Method execute = null;
		JSONArray args = null;

		try {
			int nbrKeys = (int) obj.keySet().stream().filter(s -> !s.equals("comment")).count();
			if (nbrKeys != obj.keySet().stream().filter(s -> !s.equals("comment")).distinct().count())
				throw new JsonCommandFormatException("Multiplicité d'une clé dans un argument\n\tat " + c.getSyntaxFile());

			for(String key : obj.keySet()) {
			
				if (key.equals("type")) type = obj.getString(key);
				else if (key.equals("matches")) matches = obj.getString(key);
				else if(key.equals("arguments")) args = obj.getJSONArray(key);
				else if(key.equals("keep_value")) keepValue = obj.getBoolean(key);
				else if(key.equals("tag")) tag = obj.getString(key);
				else if(key.equals("execute")) {
					String methodName = obj.getString("execute");
					execute = getMethodByName(methodName, c);
					if (execute == null)
						throw new JsonCommandFormatException("La fonction " + methodName + " n'existe pas dans la classe "
								+ c.getClass().getSimpleName());
				} else if(!key.equals("comment"))
					throw new JsonCommandFormatException("Argument invalide " + key + "\n\tat " + c.getSyntaxFile());
			}
			
			
			if(type == "")
				throw new JsonCommandFormatException("Clé \"type\" obligatoire dans la déclaration d'un argument\n\tat " + c.getSyntaxFile());
			if(tag == "")
				throw new JsonCommandFormatException("Clé \"tag\" obligatoire dans la déclaration d'un argument\n\tat " + c.getSyntaxFile());
			
			n = createNodeByType(type, matches, c);
			n.setExecute(execute);
			n.setTag(tag);
			n.setKeepValue(keepValue);
			if (args != null)
				n.setChild(parseArguments(args, c));
			return n;

		} catch (JsonCommandFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Method getMethodByName(String name, Command clazz) {
		Method[] methods = clazz.getClass().getMethods();
		for (Method method : methods)
			if (method.getName().equals(name))
				return method;
		return null;
	}

	private static Node createNodeByType(String type, String matches, Command c) throws JsonCommandFormatException {
		try {
		NodeEnum ne = NodeEnum.valueOfIgnoreCase(type);
		TypeNode tn = ne.createInstance();
		tn.parse(matches);
		return tn;
		} catch(IllegalArgumentException e) {
			throw new JsonCommandFormatException("Type d'argument \"" + type + "\" invalide\n\tat " + c.getSyntaxFile());

		}
	}
}
