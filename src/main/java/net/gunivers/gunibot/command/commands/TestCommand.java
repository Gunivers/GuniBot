package net.gunivers.gunibot.command.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.reaction.ReactionEmoji;
import net.gunivers.gunibot.az.lib.EmbedBuilder;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.event.Events;

public class TestCommand extends Command
{
	@Override public String getSyntaxFile() { return "test.json"; }
	
	public void azReact(MessageCreateEvent event)
	{
		event.getMessage().addReaction(ReactionEmoji.unicode("🤔")).subscribe();
		event.getMessage().addReaction(ReactionEmoji.unicode("🗑")).subscribe();
		
		Events.REACTION_ADDED.on(event.getMessage(), ReactionEmoji.unicode("🗑"), e -> {
			Events.REACTION_ADDED.cancel(e.getMessage().block());
			Events.REACTION_ADDED.cancel(e.getMessage().block());
			e.getMessage().block().delete().subscribe();
		});
		
		Events.REACTION_ADDED.on(event.getMessage(), ReactionEmoji.unicode("🤔"),
				e -> e.getMessage().block().getChannel().flatMap(c -> c.createMessage("Success")).subscribe());
		
		Events.REACTION_REMOVED.on(event.getMessage(),
				e -> e.getMessage().block().getChannel().flatMap(c -> c.createMessage("**PUT ME BACK!!**")).subscribe());
	}
	
	public void azEmbed(MessageCreateEvent event)
	{
		Member auth = event.getMessage().getAuthorAsMember().block();
		EmbedBuilder builder = new EmbedBuilder(event, "Test", null, auth, auth.getDefaultAvatarUrl(), null, auth.getAvatarUrl(), "Test Description",
				"Test Footer", null, auth.getDefaultAvatarUrl());
	
		for (int i = 0; i < 30; i++)
			builder.addField("Field " + i, null, true);
		
		builder.buildAndSend();
	}
}
