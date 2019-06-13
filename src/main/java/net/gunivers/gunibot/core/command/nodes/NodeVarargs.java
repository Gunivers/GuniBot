package net.gunivers.gunibot.core.command.nodes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.keys.KeyEnum;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public class NodeVarargs extends TypeNode {

	private TypeNode node;
	
	public NodeVarargs(NodeEnum ke) {
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
	
	@Override
	public Tuple2<String, String> split(String s) {
		return Tuple.newTuple(s, "");
	}

}
