package net.gunivers.gunibot.command.lib.nodes;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.interfaces.NeedGuild;
import net.gunivers.gunibot.syl2010.lib.parser.Parser;

public class NodeUser extends TypeNode<User> implements NeedGuild<User>
{
	@Override
	public void parse(String s) throws JsonCommandFormatException {}

	@Override public User getFrom(Guild guild, String s) { return Parser.parseUser(s, Main.getDiscordClient()).blockFirst(); }
	@Override public String getFrom(User u) { return u.getMention(); }

	@Override
	public CommandSyntaxError matchesNode(Guild guild, String s)
	{
		return Parser.parseUser(s, Main.getDiscordClient()).toStream().findAny().isPresent() ? null
				: new CommandSyntaxError("User `" + s + "` does not exist!");
	}
}
