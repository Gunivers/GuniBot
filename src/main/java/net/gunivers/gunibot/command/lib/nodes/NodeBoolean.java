package net.gunivers.gunibot.command.lib.nodes;

import net.gunivers.gunibot.command.lib.JsonCommandFormatException;

public class NodeBoolean extends TypeNode {

	private boolean bool;

	@Override
	protected boolean matchesNode(String s) {
		return Boolean.parseBoolean(s) == bool;
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException {
		if(s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false"))
			bool = Boolean.parseBoolean(s);
		throw new JsonCommandFormatException(s + " n'est pas de type boolean");		
	}
}
