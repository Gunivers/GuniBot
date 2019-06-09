package net.gunivers.gunibot.command.lib.nodes;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
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
	public boolean matchesNode(String s) {
		List<String> l = new LinkedList<>();
		int i = 2;
		int start = 1;
		int bracket = 2;
		if(s.startsWith("[[") && s.endsWith("]]")) {
			s = s.replaceAll(" ", "");
			while (i < s.length()) {
				if (s.charAt(i) == '[') {
					if (bracket++ == 1)
						start = i;
				} else if (s.charAt(i) == ']' && i < s.length() - 2)
					bracket--;
				else if (i == s.length() - 1 || (s.charAt(i) == ',' && i - 1 >= 0 && s.charAt(i - 1) == ']' && i + 1 < s.length() && s.charAt(i + 1) == '[')) 
					if(i - 1 != start) {
						l.add(s.substring(start, i));
					}
				i++;
			}
		} else if(s.startsWith("[") && s.endsWith("]")) {
			s = s.replaceAll(" ", "");
			s = s.substring(1, s.length() - 1);
			String[] array = s.split(",");
			if(!array[0].equals(""))
				l = Arrays.asList(array);
		} else if(!s.contains(" ") && !s.contains(",") && !s.equals(""))
			l.add(s);
		return l.size() > 0 && l.stream().allMatch(node::matchesNode);
	}
	
	@Override
	public Tuple2<String, String> split(String s) {
		s = s.trim();
		if(s.startsWith("[")) {
				int i = 1;
				int leftBracket = 1;
				int rightBracket = 0;
				while(i < s.length() && leftBracket != rightBracket) {
					if(s.charAt(i) == '[') leftBracket++;
					else if(s.charAt(i) == ']') rightBracket++;
					i++;
				}
				return Tuple.newTuple(s.substring(0, i), s.substring(i, s.length()).trim());
		} else
			super.split(s);
		return Tuple.newTuple(null, null);
	}
}
