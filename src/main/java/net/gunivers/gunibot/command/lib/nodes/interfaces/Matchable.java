package net.gunivers.gunibot.command.lib.nodes.interfaces;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;

@FunctionalInterface
public interface Matchable
{
	public CommandSyntaxError matchesNode(String s);
}
