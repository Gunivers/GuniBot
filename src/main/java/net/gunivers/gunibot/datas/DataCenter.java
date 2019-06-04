package net.gunivers.gunibot.datas;

import java.util.Map.Entry;
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
 * Centre de contrôle des données du bot.
 * @author Syl2010
 *
 */
public class DataCenter {

	private DiscordClient botClient;

	/**
	 * Contient tout les serveurs possédant des données (ou mis en cache en cas de modification de données).
	 */
	private ConcurrentHashMap<Snowflake, DataGuild> dataGuilds;

	//TODO disable:private SQLClient sql;

	public DataCenter(ReadyEvent event) {
		botClient = event.getClient();
		botClient.updatePresence(Presence.idle(Activity.watching("Loading Data Control..."))).subscribe();

		dataGuilds = new ConcurrentHashMap<>();

		//TODO disable:sql = new SQLClient();
		loadGuilds();

		registerEvents();
	}

	/**
	 * Enregistre tout les évènements nécessaire après l'initialisation du DataCenter.
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
	 * Charge les données du serveur, si existant.
	 * @param guild le serveur à chargé.
	 */
	public void addGuild(Guild guild) {
		if(hasRegisteredData(guild)) {
			//TODO disable:dataGuilds.put(guild.getId(), new DataGuild(guild, sql.loadGuildData(guild.getId().asLong())));
		}
	}

	/**
	 * Supprime les données du serveur indiqué.
	 * @param guild le serveur dont les données seront supprimés.
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
	 * Récupère les données du serveur indiqué, et le garde en cache pour enregistré les données modifiés.
	 * @param guild Le serveur.
	 * @return l'objet de donnée du serveur.
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
	 * Supprime du cache toutes les données de serveur qui ne sont pas enregistré.
	 */
	public void clearDataGuildsCache() {
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

	/**
	 * Vérifie si le serveur indiqué possède des données dans la base de donnée.
	 * @param guild le serveur à vérifier.
	 * @return {@code true} si le serveur possède des données dans la base de donnée, {@code false} sinon.
	 */
	private boolean hasRegisteredData(Guild guild) {
		//TODO disable:return sql.hasGuildData(guild.getId().asLong());
		return false;
	}

	/**
	 * Supprime les données du serveur indiqué de la base de donnée.
	 * @param guild le serveur a supprimmé de la base de donnée.
	 */
	private void removeRegisteredData(Guild guild) {
		//TODO disable:sql.removeGuildData(guild.getId().asLong());
	}

	/**
	 * Sauvegarde les données du serveur indiqué dans la base de donnée.
	 * Après sauvegarde, les données en cache dans cet objet de données sont supprimés (en réflexion).
	 * @param guild le serveur à sauvegarder.
	 */
	public void saveGuild(Guild guild) {
		DataGuild dataGuild = dataGuilds.get(guild.getId());
		if(dataGuild != null) {
			//TODO disable:sql.saveGuildData(guild.getId().asLong(), dataGuild.save());
			//dataGuild.clearAllCache();
		} else {
			System.err.println(String.format("The guild '%s' (%s) ha no data object to save!", guild.getName(), guild.getId().asString()));
		}
	}

	/**
	 * Charge les données du serveur indiqué depuis la base de donnée.
	 * @param guild le serveur à charger.
	 */
	public void loadGuild(Guild guild) {
		/* TODO disable
		if (hasRegisteredData(guild)) {
			Snowflake id = guild.getId();
			JSONObject json = sql.loadGuildData(id.asLong());
			if (dataGuilds.containsKey(id)) {
				dataGuilds.get(id).load(json);
			} else {
				dataGuilds.put(id, new DataGuild(guild, json));
			}
		} else {
			System.err.println(String.format("The guild '%s' (%s) ha no data to load!", guild.getName(), guild.getId().asString()));
		}
		 */
	}

	/**
	 * Sauvegarde les données de tout les serveurs dans la base de donnée.
	 * Le cache des serveurs est nettoyé après la sauvegarde.
	 */
	public void saveGuilds() {
		for(Entry<Snowflake, DataGuild> guild_entry:dataGuilds.entrySet()) {
			//TODO disable:sql.saveGuildData(guild_entry.getKey().asLong(), guild_entry.getValue().save());
		}
		clearDataGuildsCache();
	}

	/**
	 * Charge les données de tout les serveurs enregistrés dans la base de donnée.
	 */
	public void loadGuilds() {
		/* TODO disable
		for(Entry<Long, JSONObject> entry:sql.getAllDataGuilds().entreySet()) {
			Snowflake id = Snowflake.of(entry.getKey());
			Optional<Guild> opt_guild = botClient.getGuildById(id).blockOptional();

			if(guild_opt.isPresent()) {
				if (dataGuilds.containsKey(id)) {
					dataGuilds.get(id).load(entry.getValue());
				} else {
					dataGuilds.put(id, new DataGuild(opt_guild.get(), entry.getValue()));
				}
			} else {
				System.err.println(String.format("The guild id '%s' is not reachable! Skipping loading of this guild!", id.asString()));
			}
		}
		 */
	}

}
