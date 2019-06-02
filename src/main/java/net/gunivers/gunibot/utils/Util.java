package net.gunivers.gunibot.utils;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;

public final class Util
{
	private Util() {}
	
	public static void formatEmbed(MessageCreateEvent event, String title, EmbedCreateSpec embed)
	{
		Member author = event.getMember().get();
		User bot = event.getClient().getSelf().block();

		embed.setTitle(title);
		embed.setAuthor(bot.getUsername(), null, bot.getAvatarUrl());
		embed.setFooter("Request by " + author.getUsername(), author.getAvatarUrl());
		embed.setColor(bot.asMember(event.getGuildId().get()).block().getColor().block());
		embed.setTimestamp(event.getMessage().getTimestamp());
	}
}
