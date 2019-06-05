package net.gunivers.gunibot.command.lib.nodes;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.CommandSyntaxError.SyntaxError;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;

public class NodeString extends TypeNode<String>
{
	private String regex = ".+";

	@Override
	public CommandSyntaxError matchesNode(String s) {
		return s.matches(regex) ? null : new CommandSyntaxError(s + " should matches " + regex, SyntaxError.ARG_INVALID);
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
		else 
			return "<" + getTag() + ">" + childrenToString();
	}
	
	@Override
	public String getFrom(String s) { return s; }
}
