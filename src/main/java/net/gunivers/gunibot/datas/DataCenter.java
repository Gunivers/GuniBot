package net.gunivers.gunibot.datas;

import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import discord4j.core.DiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.BotInstance;
import net.gunivers.gunibot.core.system.RestorableSystem;
import net.gunivers.gunibot.datas.serialize.OldRestorable;
import net.gunivers.gunibot.datas.serialize.OldSerializer;
import net.gunivers.gunibot.sql.SQLRestorerSystem;
import net.gunivers.gunibot.sql.SQLRestorerSystem.SQLConfig;

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

	/**
	 * Contient la liste des anciens systèmes/objets sauvegardable (appelés par la fonction save, gèrent eux même leurs structures de données)
	 */
	private ConcurrentHashMap<String, OldRestorable> oldDataSystems;

	private SQLRestorerSystem sqlRestorerSystem;

	public DataCenter(BotInstance bot_instance) {
		botInstance = bot_instance;
		botClient = bot_instance.getBotClient();

		dataGuilds = new ConcurrentHashMap<>();
		dataUsers = new ConcurrentHashMap<>(128);

		sqlRestorerSystem = new SQLRestorerSystem(new SQLConfig(botInstance.getConfig()));
	}

	public void registerOldSerializer(String system_id, OldRestorable system) {
		oldDataSystems.put(system_id, system);
	}

	public void unregisterOldSerializer(String system_id) {
		oldDataSystems.remove(system_id);
	}

	public boolean isRegisteredOldSerializer(String system_id) {
		return sqlRestorerSystem.hasOldSerializerData(system_id);
	}

	public void registerSystem(String system_id, RestorableSystem system) {
		sqlRestorerSystem.registerSystem(system_id, system);
	}

	public void unregisterSystem(String system_id) {
		sqlRestorerSystem.unregisterSystem(system_id);
	}

	public boolean isRegisteredSystem(String system_id) {
		return sqlRestorerSystem.isRegistered(system_id);
	}

	/**
	 * Charge les données du serveur, si existant.
	 * @param guild le serveur à chargé.
	 */
	public void addGuild(Guild guild) {
		if(hasRegisteredData(guild)) {
			DataGuild data_guild = new DataGuild(guild);
			data_guild.load(sqlRestorerSystem.loadGuildData(guild.getId().asLong()));
			dataGuilds.put(guild.getId(), data_guild);
		}
		for (Member user:guild.getMembers().toIterable()) {
			if(hasRegisteredData(user) && !dataUsers.containsKey(user.getId())) {
				DataUser data_user = new DataUser(user);
				data_user.load(sqlRestorerSystem.loadUserData(user.getId().asLong()));
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
				data_guild.load(sqlRestorerSystem.loadGuildData(guild.getId().asLong()));
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
				data_user.load(sqlRestorerSystem.loadUserData(user.getId().asLong()));
			} else {
				data_user = new DataUser(user);
			}
			dataUsers.put(user.getId(), data_user);
			return data_user;
		}
	}

	/**
	 * Récupère les données de l'ancien système indiqué.
	 * @param String id du système.
	 * @return Les données, formatés en json.
	 */
	public JSONObject getDataSerializer(String system_id) {
		OldRestorable system_datas = oldDataSystems.get(system_id);
		if(system_datas != null) {
			return system_datas.save().toJson();
		} else {
			throw new IllegalArgumentException(String.format("The old system '%s' is not registered!", system_id));
		}
	}

	public RestorableSystem getSystem(String id) {
		return sqlRestorerSystem.getSystem(id);
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
		return sqlRestorerSystem.hasGuildData(guild.getId().asLong());
	}

	/**
	 * Vérifie si l'utilisateur indiqué possède des données dans la base de donnée.
	 * @param user l'utilisateur à vérifier.
	 * @return {@code true} si l'utilisateur possède des données dans la base de donnée, {@code false} sinon.
	 */
	private boolean hasRegisteredData(User user) {
		return sqlRestorerSystem.hasUserData(user.getId().asLong());
	}

	/**
	 * Vérifie si le système indiqué possède des données dans la base de donnée.
	 * @param String l'id du système à vérifier.
	 * @return {@code true} si ce système possède des données dans la base de donnée, {@code false} sinon.
	 */
	private boolean hasRegisteredData(String system_id) {
		return sqlRestorerSystem.hasSystemData(system_id);
	}

	/**
	 * Supprime les données du serveur indiqué de la base de donnée.
	 * @param guild le serveur a supprimmé de la base de donnée.
	 */
	private void removeRegisteredData(Guild guild) {
		sqlRestorerSystem.removeGuildData(guild.getId().asLong());
	}

	/**
	 * Supprime les données de l'utilisateur indiqué de la base de donnée.
	 * @param user l'utilisateur a supprimmé de la base de donnée.
	 */
	private void removeRegisteredData(User user) {
		sqlRestorerSystem.removeUserData(user.getId().asLong());
	}

	/**
	 * Sauvegarde les données du serveur indiqué dans la base de donnée.
	 * Après sauvegarde, les données en cache dans cet objet de données sont supprimés (en réflexion).
	 * @param guild le serveur à sauvegarder.
	 */
	public void saveGuild(Guild guild) {
		DataGuild dataGuild = dataGuilds.get(guild.getId());
		if(dataGuild != null) {
			sqlRestorerSystem.saveGuildData(guild.getId().asLong(), dataGuild.save());
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
			sqlRestorerSystem.saveUserData(user.getId().asLong(), dataUser.save());
			//dataGuild.clearAllCache();
		} else {
			System.err.println(String.format("The user '%s' (%s) has no data object to save!", user.getUsername(), user.getId().asString()));
		}
	}

	/**
	 * Sauvegarde les données du système indiqué dans la base de donnée.
	 * @param String id du système à sauvegarder.
	 */
	public void saveOldSerializer(String system_id) {
		OldRestorable system_datas = oldDataSystems.get(system_id);
		if(system_datas != null) {
			sqlRestorerSystem.saveOldSerializerData(system_id, system_datas.save().toJson());
		} else {
			System.err.println(String.format("The system '%s' is not registered!", system_id));
		}
	}

	/**
	 * Sauvegarde les données du système indiqué dans la base de donnée.
	 * @param String id du système à sauvegarder.
	 */
	public void saveSystem(String system_id) {
		if(sqlRestorerSystem.isRegistered(system_id)) {
			sqlRestorerSystem.saveSystem(system_id);
		} else {
			System.err.println(String.format("The system '%s' is not registered!", system_id));
		}
	}

	/**
	 * Charge les données du serveur indiqué depuis la base de donnée.
	 * @param guild le serveur à charger.
	 */
	public void loadGuild(Guild guild) {
		if (hasRegisteredData(guild)) {
			Snowflake id = guild.getId();
			JSONObject json = sqlRestorerSystem.loadGuildData(id.asLong());
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
			JSONObject json = sqlRestorerSystem.loadUserData(id.asLong());
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
	 * Charge les données du système indiqué depuis la base de donnée.
	 * @param String l'id du système à charger.
	 */
	public void loadOldSerializer(String system_id) {
		if (oldDataSystems.containsKey(system_id)) {
			if (hasRegisteredData(system_id)) {
				oldDataSystems.get(system_id).load(OldSerializer.from(sqlRestorerSystem.loadOldSerializerData(system_id)));
			} else {
				System.err.println(String.format("The system '%s' ha no data to load!", system_id));
			}
		} else {
			System.err.println(String.format("The system '%s' is not registered!", system_id));
		}

	}

	/**
	 * Charge les données du système indiqué depuis la base de donnée.
	 * @param String l'id du système à charger.
	 */
	public void loadSystem(String system_id) {
		if (sqlRestorerSystem.isRegistered(system_id)) {
			if (hasRegisteredData(system_id)) {
				sqlRestorerSystem.loadSystem(system_id);
			} else {
				System.err.println(String.format("The system '%s' ha no data to load!", system_id));
			}
		} else {
			System.err.println(String.format("The system '%s' is not registered!", system_id));
		}

	}

	/**
	 * Sauvegarde les données de tout les serveurs dans la base de donnée.
	 * Le cache des serveurs est nettoyé après la sauvegarde.
	 */
	public void saveGuilds() {
		for(Entry<Snowflake, DataGuild> guild_entry:dataGuilds.entrySet()) {
			sqlRestorerSystem.saveGuildData(guild_entry.getKey().asLong(), guild_entry.getValue().save());
		}
		clearDataGuildsCache();
	}

	/**
	 * Sauvegarde les données de tout les utilisateurs dans la base de donnée.
	 * Le cache des utilisateurs est nettoyé après la sauvegarde.
	 */
	public void saveUsers() {
		for(Entry<Snowflake, DataUser> user_entry:dataUsers.entrySet()) {
			sqlRestorerSystem.saveUserData(user_entry.getKey().asLong(), user_entry.getValue().save());
		}
		clearDataUsersCache();
	}

	/**
	 * Sauvegarde les données de tout les systèmes dans la base de donnée.
	 */
	public void saveOldSerializer() {
		for(Entry<String, OldRestorable> system_entry:oldDataSystems.entrySet()) {
			sqlRestorerSystem.saveOldSerializerData(system_entry.getKey(), system_entry.getValue().save().toJson());
		}
	}

	/**
	 * Sauvegarde les données de tout les systèmes dans la base de donnée.
	 */
	public void saveSystems() {
		for(Entry<String, RestorableSystem> system_entry:sqlRestorerSystem.getSystems()) {
			sqlRestorerSystem.saveSystem(system_entry.getKey());
		}
	}

	/**
	 * Charge les données de tout les serveurs enregistrés dans la base de donnée.
	 */
	public void loadGuilds() {
		for(Entry<Long, JSONObject> entry:sqlRestorerSystem.getAllDataGuilds().entrySet()) {
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
		for(Entry<Long, JSONObject> entry:sqlRestorerSystem.getAllDataUsers().entrySet()) {
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

	/**
	 * Charge les données de tout les systèmes enregistrés dans la base de donnée.
	 */
	public void loadOldSerializer() {
		for(Entry<String, JSONObject> entry:sqlRestorerSystem.getAllOldDataSerializer().entrySet()) {

			if(oldDataSystems.containsKey(entry.getKey())) {
				oldDataSystems.get(entry.getKey()).load(OldSerializer.from(entry.getValue()));
			} else {
				System.err.println(String.format("The old serializer id '%s' is not registered! Skipping loading of this serializer!", entry.getKey()));
			}
		}
	}

	/**
	 * Charge les données de tout les systèmes enregistrés dans la base de donnée.
	 */
	public void loadSystems() {
		for(Entry<String, RestorableSystem> entry:sqlRestorerSystem.getSystems()) {
			sqlRestorerSystem.loadSystem(entry.getKey());
		}
	}

	public void shutdown() {
		botClient.updatePresence(Presence.doNotDisturb(Activity.watching("Shutdown...")));
		saveGuilds();
		saveUsers();
		saveSystems();
		saveOldSerializer();
		botClient.logout().subscribe();
	}

}