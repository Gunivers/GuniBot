package net.gunivers.gunibot.command.lib.keys;

import java.util.stream.Collectors;

import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;
import net.gunivers.gunibot.command.lib.nodes.NodeList;

public class KeyAliases extends Key {

	private static KeyAliases instance;

	private KeyAliases() {}
	
	public static KeyAliases getInstance() {
		return instance == null ? instance = new KeyAliases() : instance;
	}

	@Override
	public String getKey() {
		return "aliases";
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		((NodeList<String>)n).addElements(obj.getJSONArray(getKey()).toList().stream().map(s -> s.toString()).collect(Collectors.toList()));
		return n;
	}
}
