package net.gunivers.gunibot.core.lib.parsing.discord;

import net.gunivers.gunibot.core.command.parser.Parser;
import net.gunivers.gunibot.core.lib.parsing.ParsingException;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.GuildChannel;

public class ChannelParser implements net.gunivers.gunibot.core.lib.parsing.Parser<GuildChannel>
{
	private Guild guild;

	@Override
	public GuildChannel parse(String input) throws ParsingException
	{
		GuildChannel channel = Parser.parseChannel(input, this.guild).blockFirst();

		if (channel == null)
			throw new ParsingException("No channel was found for argument: "+ input);

		return channel;
	}
}
