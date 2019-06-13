package net.gunivers.gunibot.core.command.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;

public class KeyTag extends Key {

	private static KeyTag instance;

	private KeyTag() {}
	
	public static KeyTag getInstance() {
		return instance == null ? instance = new KeyTag() : instance;
	}

	@Override
	public String getKey() {
		return "tag";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		n.setTag(obj.getString(getKey()));
		return n;
	}

}
