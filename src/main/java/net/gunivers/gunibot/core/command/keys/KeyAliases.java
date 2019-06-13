package net.gunivers.gunibot.core.command.keys;

import java.util.stream.Collectors;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;
import net.gunivers.gunibot.core.command.nodes.NodeRoot;

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

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		((NodeRoot)n).addAliases(obj.getJSONArray(getKey()).toList().stream().map(s -> s.toString()).collect(Collectors.toList()));
		return n;
	}
}
