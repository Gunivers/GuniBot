package net.gunivers.gunibot;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.command.lib.Command;

/**
 * Centre de contrôle des données du bot
 * @author Syl2010
 *
 */
public class DataCenter {

	private DiscordClient botClient;

	/**
	 * Contient tout les serveurs possédant des données (ou mis en cache en cas de modification de données)
	 */
	private ConcurrentHashMap<Snowflake, DataGuild> dataGuilds;

	//TODO disable:private SQLClient sql;

	public DataCenter(ReadyEvent event) {
		botClient = event.getClient();
		botClient.updatePresence(Presence.idle(Activity.watching("Loading Data Control..."))).subscribe();

		dataGuilds = new ConcurrentHashMap<>();

		//TODO disable:sql = new SQLClient();

		registerEvents();
	}

	/**
	 * Enregistre tout les évènements nécessaire après l'initialisation du DataCenter
	 */
	private void registerEvents() {
		EventDispatcher dispatcher = botClient.getEventDispatcher();

		dispatcher.on(MessageCreateEvent.class).subscribe(event -> {
			Optional<String> msg = event.getMessage().getContent();
			if(msg.isPresent() && msg.get().startsWith(Command.PREFIX)) {
				String[] cmd = msg.get().substring(Command.PREFIX.length()).split(" ");
				Command command = Command.commands.get(Command.commands.keySet().stream().filter(x -> x.contains(cmd[0])).findFirst().orElse(null));
				if(command != null)
					command.apply(cmd, event);
			}
		});
	}

	/**
	 * Charge les données du serveur, si existant
	 * @param guild le serveur à chargé
	 */
	public void addGuild(Guild guild) {
		if(hasRegisteredData(guild)) {
			dataGuilds.put(guild.getId(), new DataGuild(guild));
		}
	}

	/**
	 * Supprime les données du serveur indiqué
	 * @param guild le serveur dont les données seront supprimés
	 */
	public void removeGuild(Guild guild) {
		if(hasRegisteredData(guild)) {
			dataGuilds.remove(guild.getId());
			removeRegisteredData(guild);
		} else {
			dataGuilds.remove(guild.getId());
		}
	}

	/**
	 * Récupère les données du serveur indiqué, et le garde en cache pour enregistré les données modifiés
	 * @param guild Le serveur
	 * @return
	 */
	public DataGuild getDataGuild(Guild guild) {
		if(hasRegisteredData(guild)) {
			return dataGuilds.get(guild.getId());
		} else {
			DataGuild data_guild = new DataGuild(guild);
			dataGuilds.put(guild.getId(), data_guild);
			return data_guild;
		}
	}

	/**
	 * Supprime du cache toutes les données de serveur qui ne sont pas enregistré
	 */
	public void clearEmptyDataGuilds() {
		for(Snowflake id:dataGuilds.keySet()) {
			Optional<Guild> guild_opt = botClient.getGuildById(id).blockOptional();
			if(guild_opt.isPresent()) {
				Guild guild = guild_opt.get();
				if(!hasRegisteredData(guild)) {
					dataGuilds.remove(guild.getId());
				}
			}
		}
	}

	private boolean hasRegisteredData(Guild guild) {
		//TODO disable:return sql.hasGuildData(guild.getId().asLong());
		return false;
	}

	private void removeRegisteredData(Guild guild) {
		//TODO disable:sql.removeGuildData(guild.getId().asLong());
	}

}
