package net.gunivers.gunibot;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;

public class DataCenter {

	private DiscordClient botClient;

	public DataCenter(ReadyEvent event) {
		botClient = event.getClient();
		botClient.updatePresence(Presence.idle(Activity.watching("Loading Data Control..."))).subscribe();
	}

}
