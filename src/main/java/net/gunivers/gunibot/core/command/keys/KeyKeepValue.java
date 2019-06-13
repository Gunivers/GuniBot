package net.gunivers.gunibot.core.command.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;

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
