package net.gunivers.gunibot.command.lib.nodes;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.CommandSyntaxError.SyntaxError;

public class NodeInteger extends TypeNode<Integer>
{
	private int min = Integer.MIN_VALUE;
	private int max = Integer.MAX_VALUE;

	@Override
	public CommandSyntaxError matchesNode(String s) {
		try {
			int x = Integer.parseInt(s);
			return x >= min || x <= max ? null : new CommandSyntaxError(s +" should exist between "+ min +" and "+ max, SyntaxError.ARG_INVALID);
		} catch (NumberFormatException e) { return new CommandSyntaxError(s +" should be an integer", SyntaxError.ARG_INVALID); }
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException
	{
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
		
		if (s.matches("\\d+\\.\\."))
			min = Integer.parseInt(s.replaceAll("\\.", ""));
		else if (s.matches("\\.\\.\\d+"))
			max = Integer.parseInt(s.replaceAll("\\.", ""));
		else if (s.matches("\\d+..\\d+"))
		{
			String[] b = s.split("\\.\\.");
			min = Integer.parseInt(b[0]);
			max = Integer.parseInt(b[1]);
		} else
			throw new JsonCommandFormatException("The condition " + s + " should matches \\d+\\.\\.\\d+");
	}
	
	@Override public Integer getFrom(String s) { return Integer.parseInt(s); }
	@Override public String getFrom(Integer i) { return i.toString(); }
}