package net.gunivers.gunibot;

import java.util.Optional;

import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.commands.Command;

public class Listener {

	private DiscordClient botClient;

	public Listener(ReadyEvent event) {
		botClient = event.getClient();

		registerEvents();
	}

	/**
	 * Fonction enregistrant tout les évènements nécessaire au fonctionnement du bot
	 */
	private void registerEvents() {
		EventDispatcher dispatcher = botClient.getEventDispatcher();

		dispatcher.on(GuildCreateEvent.class).subscribe(event -> {
			//code à éxécuter lorsque le bot se connecte à un serveur discord
		});

		dispatcher.on(MessageCreateEvent.class).subscribe(event -> {
			Optional<String> msg = event.getMessage().getContent();
			if(msg.isPresent() && msg.get().startsWith(Command.PREFIX));
				
		});
	}

}
