package net.gunivers.gunibot.command.lib;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.gunivers.gunibot.command.commands.CookieCommand;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public class CommandParser {

	public static void main(String... args) {
		parseCommand(new CookieCommand());
	}

	/**
	 * Parse la syntaxe d'une commande
	 * @param c la commande dont la syntaxe doit être parsée
	 * @return la liste des alias de la commande
	 */
	public static Node parseCommand(Command c) {
		try {
			String s = Utils.getResourceFileContent("commands/", c.getSyntaxFile());
			JSONObject obj = new JSONObject(s);
			Node n = parseRoot(obj, c);
			c.setSyntax(n);
			return n;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	private static Node parseRoot(JSONObject obj, Command c) throws JSONException, Exception {
		List<String> keys = obj.keySet().stream().filter(x -> !x.equals("comment")).collect(Collectors.toList());
		if (keys.size() == 1) {
			String keyRoot = keys.get(0);
			return parseHead(obj.getJSONObject(keyRoot), keyRoot, c);
		}
		throw new JsonCommandFormatException("Racine invalide");
	}

	private static Node parseHead(JSONObject obj, String root, Command c) throws Exception {
		if (obj.keySet().stream().distinct().count() == obj.keySet().size()) {
			NodeRoot nr = new NodeRoot(root);
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
					nr.addAliases(obj.getJSONArray(key).toList().stream().map(s -> s.toString())
							.collect(Collectors.toList()));
					break;
				case "arguments":
					nr.setChild(parseArguments(obj.getJSONArray(key), c));
					break;
				case "execute":
					Method m = getMethodByName(obj.getString(key), c);
					nr.setExecute(m);
					if(m == null)
						throw new JsonCommandFormatException("La fonction " + obj.getString(key) + " n'existe pas dans la classe " + c.getClass().getSimpleName());
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
		for(int i = 0; i < array.length(); i++)
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
			if(nbrKeys != obj.keySet().stream().filter(s -> !s.equals("comment")).distinct().count())
				throw new JsonCommandFormatException("Multiplicité d'une clé dans un argument");
		
			int nbrKeysCount = 0;
			
//			if(obj.has("arguments") && obj.has("execute"))
//				throw new JsonCommandFormatException("Les clés \"execute\" et \"arguments\" ne peuvent pas être tous deux dans le même argument");
			if(obj.has("type")) {
				type = obj.getString("type");
				nbrKeysCount++; 
			} else
				throw new JsonCommandFormatException("Clé \"type\" obligatoire dans la déclaration d'un argument");
			if(obj.has("matches")) {
				if(type.equals("string"))
				matches = obj.getString("matches");
				nbrKeysCount++; 
			}
			if(obj.has("arguments")) {
				args = obj.getJSONArray("arguments");
				nbrKeysCount++; 
			}
			if(obj.has("keep_value")) {
				keepValue = obj.getBoolean("keep_value");
				nbrKeysCount++; 
			}
			if(obj.has("execute")) {
				String methodName = obj.getString("execute");
				execute = getMethodByName(methodName, c);
				if(execute == null)
					throw new JsonCommandFormatException("La fonction " + methodName + " n'existe pas dans la classe " + c.getClass().getSimpleName());
				nbrKeysCount++; 
			}
			if(nbrKeysCount != nbrKeys)
				throw new JsonCommandFormatException("Arguments invalides");
			
			n = createNodeByType(type, matches);
			n.setExecute(execute);
			n.setKeepValue(keepValue);
			if(args != null)
				n.setChild(parseArguments(args, c));
			return n;
			
		} catch (JsonCommandFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Method getMethodByName(String name, Command clazz) {
		Method[] methods = clazz.getClass().getMethods();
		for(Method method : methods)
			if(method.getName().equals(name))
				return method;
		return null;
	}

	private static Node createNodeByType(String type, String matches) throws JsonCommandFormatException {
		switch (type) {
		case "integer":
			Tuple2<Integer, Integer> bounds = parseBound(matches);
			return new NodeInt(bounds._1, bounds._2);
		case "string":
			return new NodeString(matches == "" ? ".*" : parseRegex(matches));
		case "boolean":
			return new NodeBoolean(parseBool(matches));
		case "function": 
			return new NodeInt(5, 5);
		default:
			throw new JsonCommandFormatException("Type d'argument \"" + type + "\" invalide");
		}
	}
	
	private static boolean parseBool(String s) throws JsonCommandFormatException {
		if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
			return Boolean.parseBoolean(s);
		throw new JsonCommandFormatException(s + " n'est pas de type boolean");
	}

	private static Tuple2<Integer, Integer> parseBound(String bound) throws JsonCommandFormatException {
		if(bound.equals(""))
			return Tuple.newTuple(Integer.MIN_VALUE, Integer.MAX_VALUE);
		if (bound.matches("\\d+.."))
			return Tuple.newTuple(Integer.parseInt(bound.replaceAll("\\.", "")), Integer.MAX_VALUE);
		if (bound.matches("..\\d+"))
			return Tuple.newTuple(Integer.MIN_VALUE, Integer.parseInt(bound.replaceAll("\\.", "")));
		if (bound.matches("\\d+..\\d+")) {
			String[] b = bound.split("\\.\\.");
			return Tuple.newTuple(Integer.parseInt(b[0]), Integer.parseInt(b[1]));
		}
		throw new JsonCommandFormatException("Le couple (matches, " + bound + ") ne corresponds pas au type integer");
	}

	private static String parseRegex(String s) throws JsonCommandFormatException {
		try {
			Pattern.compile(s);
			return s;
		} catch (PatternSyntaxException e) {
			throw new JsonCommandFormatException(s + " n'est pas une expression régulière valide");
		}
	}
}
