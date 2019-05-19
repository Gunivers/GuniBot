package net.gunivers.gunibot;

import java.util.concurrent.ConcurrentHashMap;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;

public class DataCenter {

	private DiscordClient botClient;
	private ConcurrentHashMap<Snowflake, DataGuild> dataGuilds;

	public DataCenter(ReadyEvent event) {
		botClient = event.getClient();
		botClient.updatePresence(Presence.idle(Activity.watching("Loading Data Control..."))).subscribe();

		dataGuilds = new ConcurrentHashMap<>();
	}

	public void addGuild(Guild guild) {
		dataGuilds.put(guild.getId(), new DataGuild(guild));
	}

}
