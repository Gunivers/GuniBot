package net.gunivers.gunibot.command.lib.keys;

import java.util.stream.Collectors;

import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;

public class KeyPermissions extends Key {

	private static KeyPermissions instance;

	private KeyPermissions() {}
	
	public static KeyPermissions getInstance() {
		return instance == null ? instance = new KeyPermissions() : instance;
	}

	@Override
	public String getKey() {
		return "permissions";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		c.addPermissions(obj.getJSONArray(getKey()).toList().stream().map(s -> s.toString()).collect(Collectors.toList()));
		return n;
	}
}
