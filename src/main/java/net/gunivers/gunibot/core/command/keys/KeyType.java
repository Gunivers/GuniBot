package net.gunivers.gunibot.core.command.keys;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;
import net.gunivers.gunibot.core.command.nodes.NodeEnum;
import net.gunivers.gunibot.core.command.nodes.NodeList;
import net.gunivers.gunibot.core.command.nodes.NodeVarargs;
import net.gunivers.gunibot.core.command.nodes.TypeNode;

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
		} else if(type.startsWith("list<") && type.endsWith(">")) {
			type = type.substring(5, type.length() - 1);
			return new NodeList((TypeNode)createNodeByType(type, pathCommand));
		}
		NodeEnum ne = NodeEnum.valueOfIgnoreCase(type);
		TypeNode tn = ne.createInstance();
		return tn;
		} catch(IllegalArgumentException e) {
			throw new JsonCommandFormatException("Type d'argument \"" + type + "\" invalide\n\tat " + pathCommand);

		}
	}

}
