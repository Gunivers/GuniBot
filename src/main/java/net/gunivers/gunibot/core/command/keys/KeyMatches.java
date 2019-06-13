package net.gunivers.gunibot.core.command.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;
import net.gunivers.gunibot.core.command.nodes.TypeNode;

public class KeyMatches extends Key {

	private static KeyMatches instance;

	private KeyMatches() {}
	
	public static KeyMatches getInstance() {
		return instance == null ? instance = new KeyMatches() : instance;
	}

	@Override
	public String getKey() {
		return "matches";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		((TypeNode) n).parse(obj.getString(getKey()));
		return n;
	}

}
