package net.gunivers.gunibot.command.lib.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;
import net.gunivers.gunibot.command.lib.nodes.NodeEnum;
import net.gunivers.gunibot.command.lib.nodes.NodeVarargs;
import net.gunivers.gunibot.command.lib.nodes.TypeNode;

public class KeyType extends Key {
	
	private static KeyType instance;

	private KeyType() {}
	
	public static KeyType getInstance() {
		return instance == null ? instance = new KeyType() : instance;
	}

	@Override
	public String getKey() {
		return "type";
	}

	@Override
	public boolean isMandatory() {
		return true;
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		Node node = createNodeByType(obj.getString(getKey()), null);
		return node;
	}
	
	private static Node createNodeByType(String type, String pathCommand) throws JsonCommandFormatException {
		try {
		if(type.endsWith("...")) {
			type = type.substring(0, type.length() - 3);
			return new NodeVarargs(NodeEnum.valueOfIgnoreCase(type)); 
		}
		NodeEnum ne = NodeEnum.valueOfIgnoreCase(type);
		TypeNode tn = ne.createInstance();
		return tn;
		} catch(IllegalArgumentException e) {
			throw new JsonCommandFormatException("Type d'argument \"" + type + "\" invalide\n\tat " + pathCommand);

		}
	}

}
