package net.gunivers.gunibot.command.lib.nodes;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.CommandSyntaxError.SyntaxError;
import net.gunivers.gunibot.command.lib.nodes.interfaces.NeedGuild;
import net.gunivers.gunibot.syl2010.lib.parser.Parser;

public class NodeTextChannel extends TypeNode<TextChannel> implements NeedGuild<TextChannel>
{
	@Override
	public void parse(String s) {}

	@Override public TextChannel getFrom(Guild guild, String s) { return Parser.parseTextChannel(s, guild).blockFirst(); }
	@Override public String getFrom(TextChannel t) { return t.getMention(); }

	@Override
	public CommandSyntaxError matchesNode(Guild guild, String s)
	{
		return Parser.parseTextChannel(s, guild).toStream().findAny().isPresent()
		? null : new CommandSyntaxError(s + " should exist!", SyntaxError.ARG_INVALID);
	}
}
