package net.gunivers.gunibot.datas;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.BotInstance;
import net.gunivers.gunibot.sql.SQLClient;
import net.gunivers.gunibot.sql.SQLClient.SQLConfig;

/**
 * Centre de contrôle des données du bot.
 * @author Syl2010
 *
 */
public class DataCenter {

	private DiscordClient botClient;
	private BotInstance botInstance;

	/**
	 * Contient tout les serveurs possédant des données (ou mis en cache en cas de modification de données).
	 */
	private ConcurrentHashMap<Snowflake, DataGuild> dataGuilds;

	/**
	 * Contient tout les utilisateurs possédant des données (ou mis en cache en cas de modification de données, synchronisé entre les serveurs).
	 */
	private ConcurrentHashMap<Snowflake, DataUser> dataUsers;

	private SQLClient sql;

	public DataCenter(ReadyEvent event, BotInstance bot_instance) {
		botInstance = bot_instance;
		botClient = event.getClient();
		botClient.updatePresence(Presence.idle(Activity.watching("Loading Data Control..."))).subscribe();

		dataGuilds = new ConcurrentHashMap<>();
		dataUsers = new ConcurrentHashMap<>(128);

		sql = new SQLClient(new SQLConfig(botInstance.getConfig()), true);
	}

	/**
	 * Charge les données du serveur, si existant.
	 * @param guild le serveur à chargé.
	 */
	public void addGuild(Guild guild) {
		if(hasRegisteredData(guild)) {
			DataGuild data_guild = new DataGuild(guild);
			data_guild.load(sql.loadGuildData(guild.getId().asLong()));
			dataGuilds.put(guild.getId(), data_guild);
		}
		for (Member user:guild.getMembers().toIterable()) {
			if(hasRegisteredData(user) && !dataUsers.containsKey(user.getId())) {
				DataUser data_user = new DataUser(user);
				data_user.load(sql.loadUserData(user.getId().asLong()));
				dataUsers.put(user.getId(), data_user);
			}
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
	 * Supprime les données de l'utilisateur indiqué.
	 * @param user l'utilisateur dont les données seront supprimés.
	 */
	public void removeUser(User user) {
		if(hasRegisteredData(user)) {
			dataUsers.remove(user.getId());
			removeRegisteredData(user);
		} else {
			dataUsers.remove(user.getId());
		}
	}

	/**
	 * Récupère les données du serveur indiqué, et le garde en cache pour enregistré les données modifiés.
	 * @param guild Le serveur.
	 * @return l'objet de donnée du serveur.
	 */
	public DataGuild getDataGuild(Guild guild) {
		if(dataGuilds.containsKey(guild.getId())) {
			return dataGuilds.get(guild.getId());
		} else {
			DataGuild data_guild;
			if(hasRegisteredData(guild)) {
				data_guild = new DataGuild(guild);
				data_guild.load(sql.loadGuildData(guild.getId().asLong()));
			} else {
				data_guild = new DataGuild(guild);
			}
			dataGuilds.put(guild.getId(), data_guild);
			return data_guild;
		}
	}

	/**
	 * Récupère les données d" l'utilisateur indiqué, et le garde en cache pour enregistré les données modifiés.
	 * @param user L'utilisateur.
	 * @return l'objet de donnée de l'utilisateur.
	 */
	public DataUser getDataUser(User user) {
		if(dataUsers.containsKey(user.getId())) {
			return dataUsers.get(user.getId());
		} else {
			DataUser data_user;
			if(hasRegisteredData(user)) {
				data_user = new DataUser(user);
				data_user.load(sql.loadUserData(user.getId().asLong()));
			} else {
				data_user = new DataUser(user);
			}
			dataUsers.put(user.getId(), data_user);
			return data_user;
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
	 * Supprime du cache toutes les données d'utilisateur qui ne sont pas enregistré.
	 */
	public void clearDataUsersCache() {
		for(Snowflake id:dataUsers.keySet()) {
			Optional<User> user_opt = botClient.getUserById(id).blockOptional();
			if(user_opt.isPresent()) {
				User user = user_opt.get();
				if(!hasRegisteredData(user)) {
					dataUsers.remove(user.getId());
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
		return sql.hasGuildData(guild.getId().asLong());
	}

	/**
	 * Vérifie si l'utilisateur indiqué possède des données dans la base de donnée.
	 * @param user l'utilisateur à vérifier.
	 * @return {@code true} si l'utilisateur possède des données dans la base de donnée, {@code false} sinon.
	 */
	private boolean hasRegisteredData(User user) {
		return sql.hasUserData(user.getId().asLong());
	}

	/**
	 * Supprime les données du serveur indiqué de la base de donnée.
	 * @param guild le serveur a supprimmé de la base de donnée.
	 */
	private void removeRegisteredData(Guild guild) {
		sql.removeGuildData(guild.getId().asLong());
	}

	/**
	 * Supprime les données de l'utilisateur indiqué de la base de donnée.
	 * @param user l'utilisateur a supprimmé de la base de donnée.
	 */
	private void removeRegisteredData(User user) {
		sql.removeUserData(user.getId().asLong());
	}

	/**
	 * Sauvegarde les données du serveur indiqué dans la base de donnée.
	 * Après sauvegarde, les données en cache dans cet objet de données sont supprimés (en réflexion).
	 * @param guild le serveur à sauvegarder.
	 */
	public void saveGuild(Guild guild) {
		DataGuild dataGuild = dataGuilds.get(guild.getId());
		if(dataGuild != null) {
			sql.saveGuildData(guild.getId().asLong(), dataGuild.save());
			//dataGuild.clearAllCache();
		} else {
			System.err.println(String.format("The guild '%s' (%s) has no data object to save!", guild.getName(), guild.getId().asString()));
		}
	}

	/**
	 * Sauvegarde les données de l'utilisateur indiqué dans la base de donnée.
	 * Après sauvegarde, les données en cache dans cet objet de données sont supprimés (en réflexion).
	 * @param user l'utilisateur à sauvegarder.
	 */
	public void saveUser(User user) {
		DataUser dataUser = dataUsers.get(user.getId());
		if(dataUser != null) {
			sql.saveUserData(user.getId().asLong(), dataUser.save());
			//dataGuild.clearAllCache();
		} else {
			System.err.println(String.format("The user '%s' (%s) has no data object to save!", user.getUsername(), user.getId().asString()));
		}
	}

	/**
	 * Charge les données du serveur indiqué depuis la base de donnée.
	 * @param guild le serveur à charger.
	 */
	public void loadGuild(Guild guild) {
		if (hasRegisteredData(guild)) {
			Snowflake id = guild.getId();
			JSONObject json = sql.loadGuildData(id.asLong());
			if (dataGuilds.containsKey(id)) {
				dataGuilds.get(id).load(json);
			} else {
				DataGuild data_guild = new DataGuild(guild);
				data_guild.load(json);
				dataGuilds.put(id, data_guild);
			}
		} else {
			System.err.println(String.format("The guild '%s' (%s) ha no data to load!", guild.getName(), guild.getId().asString()));
		}
	}

	/**
	 * Charge les données de l'utilisateur indiqué depuis la base de donnée.
	 * @param user l'utilisateur à charger.
	 */
	public void loadUser(User user) {
		if (hasRegisteredData(user)) {
			Snowflake id = user.getId();
			JSONObject json = sql.loadUserData(id.asLong());
			if (dataUsers.containsKey(id)) {
				dataUsers.get(id).load(json);
			} else {
				DataUser data_user = new DataUser(user);
				data_user.load(json);
				dataUsers.put(id, data_user);
			}
		} else {
			System.err.println(String.format("The user '%s' (%s) ha no data to load!", user.getUsername(), user.getId().asString()));
		}
	}

	/**
	 * Sauvegarde les données de tout les serveurs dans la base de donnée.
	 * Le cache des serveurs est nettoyé après la sauvegarde.
	 */
	public void saveGuilds() {
		for(Entry<Snowflake, DataGuild> guild_entry:dataGuilds.entrySet()) {
			sql.saveGuildData(guild_entry.getKey().asLong(), guild_entry.getValue().save());
		}
		clearDataGuildsCache();
	}

	/**
	 * Sauvegarde les données de tout les utilisateurs dans la base de donnée.
	 * Le cache des utilisateurs est nettoyé après la sauvegarde.
	 */
	public void saveUsers() {
		for(Entry<Snowflake, DataUser> user_entry:dataUsers.entrySet()) {
			sql.saveUserData(user_entry.getKey().asLong(), user_entry.getValue().save());
		}
		clearDataUsersCache();
	}

	/**
	 * Charge les données de tout les serveurs enregistrés dans la base de donnée.
	 */
	public void loadGuilds() {
		for(Entry<Long, JSONObject> entry:sql.getAllDataGuilds().entrySet()) {
			Snowflake id = Snowflake.of(entry.getKey());
			Optional<Guild> opt_guild = botClient.getGuildById(id).blockOptional();

			if(opt_guild.isPresent()) {
				if (dataGuilds.containsKey(id)) {
					dataGuilds.get(id).load(entry.getValue());
				} else {
					DataGuild data_guild = new DataGuild(opt_guild.get());
					data_guild.load(entry.getValue());
					dataGuilds.put(id, data_guild);
				}
			} else {
				System.err.println(String.format("The guild id '%s' is not reachable! Skipping loading of this guild!", id.asString()));
			}
		}
	}

	/**
	 * Charge les données de tout les utilisateurs enregistrés dans la base de donnée.
	 */
	public void loadUsers() {
		for(Entry<Long, JSONObject> entry:sql.getAllDataUsers().entrySet()) {
			Snowflake id = Snowflake.of(entry.getKey());
			Optional<User> opt_user = botClient.getUserById(id).blockOptional();

			if(opt_user.isPresent()) {
				if (dataUsers.containsKey(id)) {
					dataUsers.get(id).load(entry.getValue());
				} else {
					DataUser data_user = new DataUser(opt_user.get());
					data_user.load(entry.getValue());
					dataUsers.put(id, data_user);
				}
			} else {
				System.err.println(String.format("The user id '%s' is not reachable! Skipping loading of this user!", id.asString()));
			}
		}
	}

	public void shutdown() {
		botClient.updatePresence(Presence.doNotDisturb(Activity.watching("Shutdown...")));
		saveGuilds();
		saveUsers();
		botClient.logout().subscribe();
	}

}