package net.gunivers.gunibot.core.command.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;

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
