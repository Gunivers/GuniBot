package net.gunivers.gunibot.core.command.nodes;

import java.util.Arrays;
import java.util.List;

import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.keys.KeyEnum;

public class NodeBoolean extends TypeNode {


	@Override
	protected boolean matchesNode(String s) {
		return s.equalsIgnoreCase("true") || s.equalsIgnoreCase("false");
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException {}
	
	@Override
	public List<KeyEnum> blacklist() {
		return Arrays.asList(KeyEnum.MATCHES);
	}
}
