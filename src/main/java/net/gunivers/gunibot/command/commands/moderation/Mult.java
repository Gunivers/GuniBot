package net.gunivers.gunibot.command.commands.moderation;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.core.command.Command;

public class Mult extends Command {

	public void test(MessageCreateEvent e, List<String> args) {
		args.forEach(System.out::println);
	}
	
	@Override
	public String getSyntaxFile() {
		return "moderation/mult.json";
	}

}
