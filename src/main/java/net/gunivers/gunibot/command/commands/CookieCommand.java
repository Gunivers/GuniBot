package net.gunivers.gunibot.command.commands;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.command.lib.Command;

public class CookieCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "cookie.json";
	}
	
	public void giveCookie(MessageCreateEvent e, Integer count) {
		e.getMessage().getChannel().block().createMessage(count + " cookie for theo").subscribe();
	}
	
	public void giveCookieLeirof(MessageCreateEvent e, Integer count) {
		e.getMessage().getChannel().block().createMessage(count + " cookie for leirof").subscribe();
	}
	
	public void giveCookieAll(MessageCreateEvent e, Integer count) {
		e.getMessage().getChannel().block().createMessage(count + " cookie for everyone").subscribe();
	}

}