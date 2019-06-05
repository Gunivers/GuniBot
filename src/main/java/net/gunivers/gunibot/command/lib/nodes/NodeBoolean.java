package net.gunivers.gunibot.command.lib.nodes;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.CommandSyntaxError.SyntaxError;

public class NodeBoolean extends TypeNode<Boolean>
{
	@Override
	public CommandSyntaxError matchesNode(String s)
	{
		try
		{
			Boolean.parseBoolean(s);
			return null;
		} catch (Exception e) { return new CommandSyntaxError(s + " should be a boolean!", SyntaxError.ARG_INVALID); }
	}

	@Override public void parse(String s) {}
	
	@Override public Boolean getFrom(String s) { return Boolean.parseBoolean(s); }
	@Override public String getFrom(Boolean b) { return b.toString(); }
}
