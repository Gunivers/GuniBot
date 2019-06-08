package net.gunivers.gunibot.command.lib.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;

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
