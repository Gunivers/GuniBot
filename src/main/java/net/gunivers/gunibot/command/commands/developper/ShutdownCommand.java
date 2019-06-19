package net.gunivers.gunibot.command.commands.developper;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;

public class ShutdownCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "developper/shutdown.json";
	}

	public void shutdown(MessageCreateEvent event) {
		Main.shutdown();
	}

}
