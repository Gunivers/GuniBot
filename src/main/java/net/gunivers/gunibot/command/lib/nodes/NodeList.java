package net.gunivers.gunibot.command.lib.nodes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.keys.KeyEnum;

public class NodeList extends TypeNode {

	private TypeNode node;
	
	public NodeList(NodeEnum ke) {
		node = ke.createInstance();
	}

	@Override
	protected boolean matchesNode(String s) {
		boolean b = true;
		for(String element : s.split(" "))
			b &= node.matchesNode(element);
		return b;
	}
	
	@Override
	public List<KeyEnum> blacklist() {
		List<KeyEnum> blacklist = new LinkedList<>(Arrays.asList(KeyEnum.ARGUMENTS));
		blacklist.addAll(node.blacklist());
		return blacklist;
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException {
		node.parse(s);
	}

}
