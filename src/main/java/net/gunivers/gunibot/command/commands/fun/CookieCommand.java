package net.gunivers.gunibot.command.commands.fun;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.core.command.Command;

public class CookieCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "fun/cookie.json";
	}
	
	public void giveCookie(MessageCreateEvent e, List<String> args) {
		e.getMessage().getChannel().block().createMessage(args.get(0) + " cookie for theo").subscribe();
	}
	
	public void giveCookieLeirof(MessageCreateEvent e, List<String> args) {
		e.getMessage().getChannel().block().createMessage(args.get(0) + " cookie for leirof").subscribe();
	}
	
	public void giveCookieAll(MessageCreateEvent e, List<String> args) {
		e.getMessage().getChannel().block().createMessage(args.get(0) + " cookie for everyone").subscribe();
	}

}