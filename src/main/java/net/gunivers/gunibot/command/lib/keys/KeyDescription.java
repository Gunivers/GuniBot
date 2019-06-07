package net.gunivers.gunibot.command.lib.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;

public class KeyDescription extends Key {

	private static KeyDescription instance;

	private KeyDescription() {}
	
	public static KeyDescription getInstance() {
		return instance == null ? instance = new KeyDescription() : instance;
	}
	
	@Override
	public boolean isMandatory() {
		return true;
	}

	@Override
	public String getKey() {
		return "description";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		c.setDescription(obj.getString(getKey()));
		return n;
	}

}
