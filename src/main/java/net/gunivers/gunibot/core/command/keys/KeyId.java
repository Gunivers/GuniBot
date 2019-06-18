package net.gunivers.gunibot.core.command.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;

public class KeyId extends Key {

	private static KeyId instance;

	private KeyId() {}
	
	public static KeyId getInstance() {
		return instance == null ? instance = new KeyId() : instance;
	}

	@Override
	public String getKey() {
		return "id";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		String id = obj.getString(getKey());
		if(c.isReferenced(id))
			throw new JsonCommandFormatException("ID " + id + " doit être unique\n\tat " + c.getSyntaxFile());
		n.setId(id);
		c.addIdReference(id, n);
		return n;
	}
}
