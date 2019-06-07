package net.gunivers.gunibot.command.lib.keys;

import org.json.JSONArray;
import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.CommandParser;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;

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
