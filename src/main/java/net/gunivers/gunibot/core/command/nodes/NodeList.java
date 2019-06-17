package net.gunivers.gunibot.core.command.nodes;

import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public class NodeList extends TypeNode {

	TypeNode node;
	
	public NodeList(TypeNode n) {
		node = n;
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException {
		node.parse(s);
	}

	@Override
	protected boolean matchesNode(String s)
	{
		return net.gunivers.gunibot.core.az.lib.SimpleParser.parseList(s).stream().allMatch(node::matchesNode);
	}
	
	@Override
	public Tuple2<String, String> split(String s) {
		s = s.trim();
		
		if(s.startsWith("["))
		{
			int i = 1;
			int leftBracket = 1;
			int rightBracket = 0;
			
			while(i < s.length() && leftBracket != rightBracket)
			{
				if(s.charAt(i) == '[') leftBracket++;
				else if(s.charAt(i) == ']') rightBracket++;
				i++;
			}
			
			return Tuple.newTuple(s.substring(0, i), s.substring(i, s.length()).trim());
		} else
			return super.split(s);
	}
}
