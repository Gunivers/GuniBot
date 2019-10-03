package net.gunivers.gunibot.core.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

//import net.gunivers.gunibot.command.commands.moderation.Mult;
import net.gunivers.gunibot.core.command.keys.KeyEnum;
import net.gunivers.gunibot.core.command.keys.KeyEnum.Position;
import net.gunivers.gunibot.core.command.nodes.Node;
import net.gunivers.gunibot.core.command.nodes.NodeRoot;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;

public class CommandParser {

    private static Command command;
    private static Set<String> referencedId = new HashSet<>();

    /**
     * Parse la syntaxe d'une commande
     * 
     * @param newCommand la commande dont la syntaxe doit être parsée
     * @return la liste des alias de la commande
     */
    public static NodeRoot parseCommand(Command newCommand) {
	try {
	    referencedId.clear();
	    command = newCommand;
	    String string = Utils.getResourceFileContent("commands/", newCommand.getSyntaxFile());
	    JSONObject json = new JSONObject(string);
	    NodeRoot nodeRoot = parseRoot(json);
	    newCommand.setSyntax(nodeRoot);
	    SetView<String> difference = Sets.symmetricDifference(referencedId, command.getReferences());
	    // if(c instanceof Mult)
	    // referencedId.forEach(System.out::println);
	    if (!difference.isEmpty())
		throw new JsonCommandFormatException("IDs non référencés ou non utilisés : "
			+ difference.stream().collect(Collectors.joining(", ")) + "\n\tat " + command.getSyntaxFile());
	    command = null;
	    return nodeRoot;
	} catch (Exception e) {
	    e.printStackTrace();
	    command = null;
	    return null;
	}
    }

    private static NodeRoot parseRoot(JSONObject json) throws Exception {
	List<String> keys = json.keySet().stream().filter(string -> !string.equals("comment"))
		.collect(Collectors.toList());
	if (keys.size() == 1) {
	    String keyRoot = keys.get(0);
	    return parseHead(json.getJSONObject(keyRoot), keyRoot);
	}
	throw new JsonCommandFormatException("Racine invalide\n\tat " + command.getSyntaxFile());
    }

    private static NodeRoot parseHead(JSONObject json, String root) throws Exception {
	if (json.keySet().stream().distinct().count() == json.keySet().size()) {
	    NodeRoot nr = new NodeRoot(root);
	    return (NodeRoot) parseArgument(json, nr, Position.ONLY_IN_ROOT, Position.DEFAULT);
	}
	throw new JsonCommandFormatException("En-tête de fichier invalide\n\tat " + command.getSyntaxFile());
    }

    public static List<Node> parseArguments(JSONArray jsonArray) throws JSONException, JsonCommandFormatException {
	List<Node> list = new LinkedList<>();
	for (int i = 0; i < jsonArray.length(); i++) {
	    list.add(parseArgument(jsonArray.getJSONObject(i), null, Position.NOT_IN_ROOT, Position.DEFAULT));
	}
	return list;
    }

    private static Node parseArgument(JSONObject json, Node node, Position position1, Position position2)
	    throws JsonCommandFormatException {

	int nbrKeys = (int) json.keySet().stream().filter(string -> !string.equals("comment")).count();
	if (nbrKeys != json.keySet().stream().filter(string -> !string.equals("comment")).distinct().count())
	    throw new JsonCommandFormatException(
		    "Multiplicité d'une clé dans un argument\n\tat " + command.getSyntaxFile());

	Map<String, KeyEnum> keysPresent = new HashMap<>();
	keysPresent.put("comment", null);
	for (KeyEnum keyEnum : KeyEnum.getByPos(position1, position2)) {
	    Tuple2<String, Node> result = keyEnum.getClazz().parseJson(json, node, command);
	    if (result.value1 != null) {
		keysPresent.put(result.value1, keyEnum);
	    }
	    node = result.value2;
	}
	List<KeyEnum> list = node.blacklist().stream().filter(ke -> keysPresent.containsValue(ke))
		.collect(Collectors.toList());
	if (list.size() > 0)
	    throw new JsonCommandFormatException(node.getTag() + " est incompatible avec les clés : "
		    + list.stream().map(keyEnum -> keyEnum.getClazz().getKey()).collect(Collectors.joining(", "))
		    + "\n\tat " + command.getSyntaxFile());

	list = node.mandatory().stream().filter(keyEnum -> !keysPresent.containsValue(keyEnum))
		.collect(Collectors.toList());
	if (list.size() > 0)
	    throw new JsonCommandFormatException(node.getTag() + " doit obligatoirement être avec les clés : "
		    + list.stream().map(keyEnum -> keyEnum.getClazz().getKey()).collect(Collectors.joining(", "))
		    + "\n\tat " + command.getSyntaxFile());

	String invalidKeys = json.keySet().stream().filter(string -> !keysPresent.containsKey(string))
		.collect(Collectors.joining(", "));
	if (!invalidKeys.equals(""))
	    throw new JsonCommandFormatException(
		    "Clé(s) invalide(s) " + invalidKeys + "\n\tat " + command.getSyntaxFile());

	return node;

    }

    public static void addReference(String string) {
	referencedId.add(string);
    }
}
