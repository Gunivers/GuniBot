package net.gunivers.gunibot.command.lib;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gunivers.gunibot.command.commands.CookieCommand;
import net.gunivers.gunibot.command.lib.nodes.Node;
import net.gunivers.gunibot.command.lib.nodes.NodeEnum;
import net.gunivers.gunibot.command.lib.nodes.NodeList;
import net.gunivers.gunibot.command.lib.nodes.TypeNode;

public class CommandParser {

	public static void main(String... args) {
		parseCommand(new CookieCommand());
	}

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
		throw new JsonCommandFormatException("Racine invalide");
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
					throw new JsonCommandFormatException("Clé d'en-tête \"" + key + "\" invalide");
				}
			}
			if (c.getDescription() != "")
				return nr;
		}
		throw new JsonCommandFormatException("En-tête de fichier invalide");
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
		boolean keepValue = false;
		Method execute = null;
		JSONArray args = null;

		try {
			int nbrKeys = (int) obj.keySet().stream().filter(s -> !s.equals("comment")).count();
			if (nbrKeys != obj.keySet().stream().filter(s -> !s.equals("comment")).distinct().count())
				throw new JsonCommandFormatException("Multiplicité d'une clé dans un argument");

			int nbrKeysCount = 0;

			if (obj.has("type")) {
				type = obj.getString("type");
				nbrKeysCount++;
			} else
				throw new JsonCommandFormatException("Clé \"type\" obligatoire dans la déclaration d'un argument");
			if (obj.has("matches")) {
				// if(type.equals("string"))
				matches = obj.getString("matches");
				nbrKeysCount++;
			}
			if (obj.has("arguments")) {
				args = obj.getJSONArray("arguments");
				nbrKeysCount++;
			}
			if (obj.has("keep_value")) {
				keepValue = obj.getBoolean("keep_value");
				nbrKeysCount++;
			}
			if (obj.has("execute")) {
				String methodName = obj.getString("execute");
				execute = getMethodByName(methodName, c);
				if (execute == null)
					throw new JsonCommandFormatException("La fonction " + methodName + " n'existe pas dans la classe "
							+ c.getClass().getSimpleName());
				nbrKeysCount++;
			}
			if (nbrKeysCount != nbrKeys)
				throw new JsonCommandFormatException("Arguments invalides");

			n = createNodeByType(type, matches);
			n.setExecute(execute);
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

	private static Node createNodeByType(String type, String matches) throws JsonCommandFormatException {
		try {
		NodeEnum ne = NodeEnum.valueOfIgnoreCase(type);
		TypeNode tn = ne.createInstance();
		tn.parse(matches);
		return tn;
		} catch(IllegalArgumentException e) {
			throw new JsonCommandFormatException("Type d'argument \"" + type + "\" invalide");

		}
	}
}
