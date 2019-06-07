package net.gunivers.gunibot.command.lib.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;

public class KeyKeepValue extends Key {

	private static KeyKeepValue instance;

	private KeyKeepValue() {}
	
	public static KeyKeepValue getInstance() {
		return instance == null ? instance = new KeyKeepValue() : instance;
	}

	@Override
	public String getKey() {
		return "keep_value";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		n.setKeepValue(obj.getBoolean(getKey()));
		return n;
	}
}
