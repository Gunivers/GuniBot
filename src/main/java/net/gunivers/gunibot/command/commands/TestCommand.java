package net.gunivers.gunibot.command.commands;

import java.io.IOException;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.core.BotUtils;
import net.gunivers.gunibot.core.command.Command;

/**
 * This command intents to run unit tests, and is should not interfere with any kind of data within the bot.
 * @authors A~Z
 *
 */
public class TestCommand extends Command
{
	@Override public String getSyntaxFile() { return "test.json"; }
	
	public void test2(MessageCreateEvent event) {
		String latex = "à partir de $$\\sqrt{dx^2 + dy^2}$$ ils factorisent par $$dx^2$$ et ça donne $$\\sqrt{dx^{2} * (1 + \\frac{dy^2}{dx^2})}$$";
	}
	
	public void test(MessageCreateEvent event) {
			BotUtils.sendMessageWithIdentity(event.getMessage().getAuthorAsMember().block(), event.getMessage().getChannel().block(), "Test !");
	}
}
