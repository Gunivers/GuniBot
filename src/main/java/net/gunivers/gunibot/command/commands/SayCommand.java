package net.gunivers.gunibot.command.commands;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.command.lib.Command;

public class SayCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "say.json";
	}

	public void say(MessageCreateEvent e, List<String> args) {
		System.out.println("say function");
		String result = String.join(" ", args);
		e.getMessage().getChannel().block().createMessage(result).subscribe();
	}

	public void say2(MessageCreateEvent e, List<String> args) {
		System.out.println("say2 function");
		String result = String.join(" ", args);
		e.getMessage().getChannel().block().createMessage(result).subscribe();
	}

}