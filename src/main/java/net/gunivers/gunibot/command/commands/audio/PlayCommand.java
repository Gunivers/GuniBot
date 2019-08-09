package net.gunivers.gunibot.command.commands.audio;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.audio.Audio;
import net.gunivers.gunibot.core.command.Command;

public class PlayCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "audio/play.json";
	}
	
	public void play(MessageCreateEvent e, List<String> args) {
		if (!args.isEmpty()) {
			Audio.playerManager.loadItem(args.get(0), Audio.scheduler);
		}
	}
}