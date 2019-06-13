package net.gunivers.gunibot.core.command.keys;

import org.json.JSONArray;
import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.CommandParser;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;

public class KeyArguments extends Key {

	private static KeyArguments instance;

	private KeyArguments() {}
	
	public static KeyArguments getInstance() {
		return instance == null ? instance = new KeyArguments() : instance;
	}

	@Override
	public String getKey() {
		return "arguments";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		JSONArray array = obj.getJSONArray(getKey());
		if(array != null)
			n.setChildren(CommandParser.parseArguments(array));
		return n;
	}
}
