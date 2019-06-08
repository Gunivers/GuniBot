package net.gunivers.gunibot.command.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.event.Events;

public class TestCommand extends Command
{
	@Override public String getSyntaxFile() { return "test.json"; }
	
	public void azReact(MessageCreateEvent event) throws InterruptedException
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
}
