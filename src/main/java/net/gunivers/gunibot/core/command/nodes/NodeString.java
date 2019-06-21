package net.gunivers.gunibot.core.command.nodes;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.gunivers.gunibot.core.command.JsonCommandFormatException;

public class NodeString extends TypeNode {

	private String regex = ".*";

	@Override
	protected boolean matchesNode(String s) {
		return s.matches(regex);
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException {
		try {
			Pattern.compile(s);
			regex = s;
		} catch (PatternSyntaxException e) {
			throw new JsonCommandFormatException(s + " n'est pas une expression régulière valide");
		}
	}
	
	@Override
	public String toString() {
		if(regex.matches("(\\w|\\d)*"))
			return regex + childrenToString();
		else if (regex.matches("(\\w|\\d|\\|)*"))
			return getTag() + childrenToString();
		else 
			return "<" + getTag() + ">" + childrenToString();
	}
}
