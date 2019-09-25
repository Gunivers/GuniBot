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
 * 
 * @author Syl2010
 *
 */
public class DataCenter {

    private DiscordClient botClient;
    // private BotInstance botInstance;

    /**
     * Contient tout les serveurs possédant des données (ou mis en cache en cas de
     * modification de données).
     */
    private ConcurrentHashMap<Snowflake, DataGuild> dataGuilds;

    /**
     * Contient tout les utilisateurs possédant des données (ou mis en cache en cas
     * de modification de données, synchronisé entre les serveurs).
     */
    private ConcurrentHashMap<Snowflake, DataUser> dataUsers;

    /**
     * Contient la liste des anciens systèmes/objets sauvegardable (appelés par la
     * fonction save, gèrent eux même leurs structures de données)
     */
    private ConcurrentHashMap<String, OldRestorable> oldDataSystems;

    /**
     * Système SQL gérant la sauvegarde des autres systèmes et données
     */
    private SQLRestorerSystem sqlRestorerSystem;

    public DataCenter(BotInstance newBotInstance) {
	// botInstance = newBotInstance;
	botClient = newBotInstance.getBotClient();

	dataGuilds = new ConcurrentHashMap<>();
	dataUsers = new ConcurrentHashMap<>(128);
	oldDataSystems = new ConcurrentHashMap<>();

	sqlRestorerSystem = new SQLRestorerSystem(new SQLConfig(newBotInstance.getConfig()));
    }

    /**
     * Enregistre un système utilisant l'ancien serializer
     * 
     * @param systemId l'id du système à utiliser
     * @param system   le système à enregitrer
     */
    public void registerOldSerializer(String systemId, OldRestorable system) {
	oldDataSystems.put(systemId, system);
    }

    /**
     * Désenregistre un système utilisant l'ancien serializer
     * 
     * @param systemId l'id du système à désenregistrer
     */
    public void unregisterOldSerializer(String systemId) {
	oldDataSystems.remove(systemId);
    }

    /**
     * Vérifie si le système indiqué est enregistré avec l'ancien serializer
     * 
     * @param systemId l'id du système à vérifier
     * @return {@code true} si un système est bien enregistréavec cet id,
     *         {@code false} sinon
     */
    public boolean isRegisteredOldSerializer(String systemId) {
	return sqlRestorerSystem.hasOldSerializerData(systemId);
    }

    /**
     * Enregistre un système
     * 
     * @param systemId l'id du système
     * @param system   le système à enregistré
     */
    public void registerSystem(String systemId, RestorableSystem system) {
	sqlRestorerSystem.registerSystem(systemId, system);
    }

    /**
     * Désenregistre un système
     * 
     * @param systemId l'id du système à désenregistré
     */
    public void unregisterSystem(String systemId) {
	sqlRestorerSystem.unregisterSystem(systemId);
    }

    /**
     * Vérifie si un système est enregistré avec cet id
     * 
     * @param systemId l'id du système à vérifier
     * @return {@code true} si ce système est bien enregistré, {@code false} sinon
     */
    public boolean isRegisteredSystem(String systemId) {
	return sqlRestorerSystem.isRegistered(systemId);
    }

    /**
     * Charge les données du serveur, si existant.
     * 
     * @param guild le serveur à chargé.
     */
    public void addGuild(Guild guild) {
	if (hasRegisteredData(guild)) {
	    DataGuild dataGuild = new DataGuild(guild);
	    dataGuild.load(sqlRestorerSystem.loadGuildData(guild.getId().asLong()));
	    dataGuilds.put(guild.getId(), dataGuild);
	}
	for (Member user : guild.getMembers().toIterable()) {
	    if (hasRegisteredData(user) && !dataUsers.containsKey(user.getId())) {
		DataUser dataUser = new DataUser(user);
		dataUser.load(sqlRestorerSystem.loadUserData(user.getId().asLong()));
		dataUsers.put(user.getId(), dataUser);
	    }
	}
    }

    /**
     * Supprime les données du serveur indiqué.
     * 
     * @param guild le serveur dont les données seront supprimés.
     */
    public void removeGuild(Guild guild) {
	if (hasRegisteredData(guild)) {
	    dataGuilds.remove(guild.getId());
	    removeRegisteredData(guild);
	} else {
	    dataGuilds.remove(guild.getId());
	}
    }

    /**
     * Supprime les données de l'utilisateur indiqué.
     * 
     * @param user l'utilisateur dont les données seront supprimés.
     */
    public void removeUser(User user) {
	if (hasRegisteredData(user)) {
	    dataUsers.remove(user.getId());
	    removeRegisteredData(user);
	} else {
	    dataUsers.remove(user.getId());
	}
    }

    /**
     * Récupère les données du serveur indiqué, et le garde en cache pour enregistré
     * les données modifiés.
     * 
     * @param guild Le serveur.
     * @return l'objet de donnée du serveur.
     */
    public DataGuild getDataGuild(Guild guild) {
	if (dataGuilds.containsKey(guild.getId()))
	    return dataGuilds.get(guild.getId());
	else {
	    DataGuild dataGuild;
	    if (hasRegisteredData(guild)) {
		dataGuild = new DataGuild(guild);
		dataGuild.load(sqlRestorerSystem.loadGuildData(guild.getId().asLong()));
	    } else {
		dataGuild = new DataGuild(guild);
	    }
	    dataGuilds.put(guild.getId(), dataGuild);
	    return dataGuild;
	}
    }

    /**
     * Récupère les données d" l'utilisateur indiqué, et le garde en cache pour
     * enregistré les données modifiés.
     * 
     * @param user L'utilisateur.
     * @return l'objet de donnée de l'utilisateur.
     */
    public DataUser getDataUser(User user) {
	if (dataUsers.containsKey(user.getId()))
	    return dataUsers.get(user.getId());
	else {
	    DataUser dataUser;
	    if (hasRegisteredData(user)) {
		dataUser = new DataUser(user);
		dataUser.load(sqlRestorerSystem.loadUserData(user.getId().asLong()));
	    } else {
		dataUser = new DataUser(user);
	    }
	    dataUsers.put(user.getId(), dataUser);
	    return dataUser;
	}
    }

    /**
     * Récupère les données de l'ancien système indiqué.
     * 
     * @param String id du système.
     * @return Les données, formatés en json.
     */
    public JSONObject getDataSerializer(String systemId) {
	OldRestorable systemDatas = oldDataSystems.get(systemId);
	if (systemDatas != null)
	    return systemDatas.save().toJson();
	else
	    throw new IllegalArgumentException(String.format("The old system '%s' is not registered!", systemId));
    }

    /**
     * Recupère le système enregistré avec cet id
     * 
     * @param id l'id du système à récupérer
     * @return le système enregistré avec cet id
     */
    public RestorableSystem getSystem(String id) {
	return sqlRestorerSystem.getSystem(id);
    }

    /**
     * Supprime du cache toutes les données de serveur qui ne sont pas enregistré.
     */
    public void clearDataGuildsCache() {
	for (Snowflake id : dataGuilds.keySet()) {
	    Optional<Guild> guildOpt = botClient.getGuildById(id).blockOptional();
	    if (guildOpt.isPresent()) {
		Guild guild = guildOpt.get();
		if (!hasRegisteredData(guild)) {
		    dataGuilds.remove(guild.getId());
		}
	    }
	}
    }

    /**
     * Supprime du cache toutes les données d'utilisateur qui ne sont pas
     * enregistré.
     */
    public void clearDataUsersCache() {
	for (Snowflake id : dataUsers.keySet()) {
	    Optional<User> userOpt = botClient.getUserById(id).blockOptional();
	    if (userOpt.isPresent()) {
		User user = userOpt.get();
		if (!hasRegisteredData(user)) {
		    dataUsers.remove(user.getId());
		}
	    }
	}
    }

    /**
     * Vérifie si le serveur indiqué possède des données dans la base de donnée.
     * 
     * @param guild le serveur à vérifier.
     * @return {@code true} si le serveur possède des données dans la base de
     *         donnée, {@code false} sinon.
     */
    private boolean hasRegisteredData(Guild guild) {
	return sqlRestorerSystem.hasGuildData(guild.getId().asLong());
    }

    /**
     * Vérifie si l'utilisateur indiqué possède des données dans la base de donnée.
     * 
     * @param user l'utilisateur à vérifier.
     * @return {@code true} si l'utilisateur possède des données dans la base de
     *         donnée, {@code false} sinon.
     */
    private boolean hasRegisteredData(User user) {
	return sqlRestorerSystem.hasUserData(user.getId().asLong());
    }

    /**
     * Vérifie si le système indiqué possède des données dans la base de donnée.
     * 
     * @param String l'id du système à vérifier.
     * @return {@code true} si ce système possède des données dans la base de
     *         donnée, {@code false} sinon.
     */
    private boolean hasRegisteredData(String systemId) {
	return sqlRestorerSystem.hasSystemData(systemId);
    }

    /**
     * Supprime les données du serveur indiqué de la base de donnée.
     * 
     * @param guild le serveur a supprimmé de la base de donnée.
     */
    private void removeRegisteredData(Guild guild) {
	sqlRestorerSystem.removeGuildData(guild.getId().asLong());
    }

    /**
     * Supprime les données de l'utilisateur indiqué de la base de donnée.
     * 
     * @param user l'utilisateur a supprimmé de la base de donnée.
     */
    private void removeRegisteredData(User user) {
	sqlRestorerSystem.removeUserData(user.getId().asLong());
    }

    /**
     * Sauvegarde les données du serveur indiqué dans la base de donnée. Après
     * sauvegarde, les données en cache dans cet objet de données sont supprimés (en
     * réflexion).
     * 
     * @param guild le serveur à sauvegarder.
     */
    public void saveGuild(Guild guild) {
	DataGuild dataGuild = dataGuilds.get(guild.getId());
	if (dataGuild != null) {
	    sqlRestorerSystem.saveGuildData(guild.getId().asLong(), dataGuild.save());
	    // dataGuild.clearAllCache();
	} else {
	    System.err.println(String.format("The guild '%s' (%s) has no data object to save!", guild.getName(),
		    guild.getId().asString()));
	}
    }

    /**
     * Sauvegarde les données de l'utilisateur indiqué dans la base de donnée. Après
     * sauvegarde, les données en cache dans cet objet de données sont supprimés (en
     * réflexion).
     * 
     * @param user l'utilisateur à sauvegarder.
     */
    public void saveUser(User user) {
	DataUser dataUser = dataUsers.get(user.getId());
	if (dataUser != null) {
	    sqlRestorerSystem.saveUserData(user.getId().asLong(), dataUser.save());
	    // dataGuild.clearAllCache();
	} else {
	    System.err.println(String.format("The user '%s' (%s) has no data object to save!", user.getUsername(),
		    user.getId().asString()));
	}
    }

    /**
     * Sauvegarde les données du système indiqué dans la base de donnée.
     * 
     * @param String id du système à sauvegarder.
     */
    public void saveOldSerializer(String systemId) {
	OldRestorable systemDatas = oldDataSystems.get(systemId);
	if (systemDatas != null) {
	    sqlRestorerSystem.saveOldSerializerData(systemId, systemDatas.save().toJson());
	} else {
	    System.err.println(String.format("The system '%s' is not registered!", systemId));
	}
    }

    /**
     * Sauvegarde les données du système indiqué dans la base de donnée.
     * 
     * @param String id du système à sauvegarder.
     */
    public void saveSystem(String systemId) {
	if (sqlRestorerSystem.isRegistered(systemId)) {
	    sqlRestorerSystem.saveSystem(systemId);
	} else {
	    System.err.println(String.format("The system '%s' is not registered!", systemId));
	}
    }

    /**
     * Charge les données du serveur indiqué depuis la base de donnée.
     * 
     * @param guild le serveur à charger.
     */
    public void loadGuild(Guild guild) {
	if (hasRegisteredData(guild)) {
	    Snowflake id = guild.getId();
	    JSONObject json = sqlRestorerSystem.loadGuildData(id.asLong());
	    if (dataGuilds.containsKey(id)) {
		dataGuilds.get(id).load(json);
	    } else {
		DataGuild dataGuild = new DataGuild(guild);
		dataGuild.load(json);
		dataGuilds.put(id, dataGuild);
	    }
	} else {
	    System.err.println(String.format("The guild '%s' (%s) ha no data to load!", guild.getName(),
		    guild.getId().asString()));
	}
    }

    /**
     * Charge les données de l'utilisateur indiqué depuis la base de donnée.
     * 
     * @param user l'utilisateur à charger.
     */
    public void loadUser(User user) {
	if (hasRegisteredData(user)) {
	    Snowflake id = user.getId();
	    JSONObject json = sqlRestorerSystem.loadUserData(id.asLong());
	    if (dataUsers.containsKey(id)) {
		dataUsers.get(id).load(json);
	    } else {
		DataUser dataUser = new DataUser(user);
		dataUser.load(json);
		dataUsers.put(id, dataUser);
	    }
	} else {
	    System.err.println(String.format("The user '%s' (%s) ha no data to load!", user.getUsername(),
		    user.getId().asString()));
	}
    }

    /**
     * Charge les données du système indiqué depuis la base de donnée.
     * 
     * @param String l'id du système à charger.
     */
    public void loadOldSerializer(String systemId) {
	if (oldDataSystems.containsKey(systemId)) {
	    if (hasRegisteredData(systemId)) {
		oldDataSystems.get(systemId)
			.load(OldSerializer.from(sqlRestorerSystem.loadOldSerializerData(systemId)));
	    } else {
		System.err.println(String.format("The system '%s' ha no data to load!", systemId));
	    }
	} else {
	    System.err.println(String.format("The system '%s' is not registered!", systemId));
	}

    }

    /**
     * Charge les données du système indiqué depuis la base de donnée.
     * 
     * @param String l'id du système à charger.
     */
    public void loadSystem(String systemId) {
	if (sqlRestorerSystem.isRegistered(systemId)) {
	    if (hasRegisteredData(systemId)) {
		sqlRestorerSystem.loadSystem(systemId);
	    } else {
		System.err.println(String.format("The system '%s' ha no data to load!", systemId));
	    }
	} else {
	    System.err.println(String.format("The system '%s' is not registered!", systemId));
	}

    }

    /**
     * Sauvegarde les données de tout les serveurs dans la base de donnée. Le cache
     * des serveurs est nettoyé après la sauvegarde.
     */
    public void saveGuilds() {
	for (Entry<Snowflake, DataGuild> guildEntry : dataGuilds.entrySet()) {
	    sqlRestorerSystem.saveGuildData(guildEntry.getKey().asLong(), guildEntry.getValue().save());
	}
	clearDataGuildsCache();
    }

    /**
     * Sauvegarde les données de tout les utilisateurs dans la base de donnée. Le
     * cache des utilisateurs est nettoyé après la sauvegarde.
     */
    public void saveUsers() {
	for (Entry<Snowflake, DataUser> userEntry : dataUsers.entrySet()) {
	    sqlRestorerSystem.saveUserData(userEntry.getKey().asLong(), userEntry.getValue().save());
	}
	clearDataUsersCache();
    }

    /**
     * Sauvegarde les données de tout les systèmes dans la base de donnée.
     */
    public void saveOldSerializer() {
	for (Entry<String, OldRestorable> systemEntry : oldDataSystems.entrySet()) {
	    sqlRestorerSystem.saveOldSerializerData(systemEntry.getKey(), systemEntry.getValue().save().toJson());
	}
    }

    /**
     * Sauvegarde les données de tout les systèmes dans la base de donnée.
     */
    public void saveSystems() {
	for (Entry<String, RestorableSystem> systemEntry : sqlRestorerSystem.getSystems()) {
	    sqlRestorerSystem.saveSystem(systemEntry.getKey());
	}
    }

    /**
     * Charge les données de tout les serveurs enregistrés dans la base de donnée.
     */
    public void loadGuilds() {
	for (Entry<Long, JSONObject> entry : sqlRestorerSystem.getAllDataGuilds().entrySet()) {
	    Snowflake id = Snowflake.of(entry.getKey());
	    Optional<Guild> optGuild = botClient.getGuildById(id).blockOptional();

	    if (optGuild.isPresent()) {
		if (dataGuilds.containsKey(id)) {
		    dataGuilds.get(id).load(entry.getValue());
		} else {
		    DataGuild dataGuild = new DataGuild(optGuild.get());
		    dataGuild.load(entry.getValue());
		    dataGuilds.put(id, dataGuild);
		}
	    } else {
		System.err.println(String.format("The guild id '%s' is not reachable! Skipping loading of this guild!",
			id.asString()));
	    }
	}
    }

    /**
     * Charge les données de tout les utilisateurs enregistrés dans la base de
     * donnée.
     */
    public void loadUsers() {
	for (Entry<Long, JSONObject> entry : sqlRestorerSystem.getAllDataUsers().entrySet()) {
	    Snowflake id = Snowflake.of(entry.getKey());
	    Optional<User> optUser = botClient.getUserById(id).blockOptional();

	    if (optUser.isPresent()) {
		if (dataUsers.containsKey(id)) {
		    dataUsers.get(id).load(entry.getValue());
		} else {
		    DataUser dataUser = new DataUser(optUser.get());
		    dataUser.load(entry.getValue());
		    dataUsers.put(id, dataUser);
		}
	    } else {
		System.err.println(String.format("The user id '%s' is not reachable! Skipping loading of this user!",
			id.asString()));
	    }
	}
    }

    /**
     * Charge les données de tout les systèmes enregistrés dans la base de donnée.
     */
    public void loadOldSerializer() {
	for (Entry<String, JSONObject> entry : sqlRestorerSystem.getAllOldDataSerializer().entrySet()) {

	    if (oldDataSystems.containsKey(entry.getKey())) {
		oldDataSystems.get(entry.getKey()).load(OldSerializer.from(entry.getValue()));
	    } else {
		System.err.println(String.format(
			"The old serializer id '%s' is not registered! Skipping loading of this serializer!",
			entry.getKey()));
	    }
	}
    }

    /**
     * Charge les données de tout les systèmes enregistrés dans la base de donnée.
     */
    public void loadSystems() {
	for (Entry<String, RestorableSystem> entry : sqlRestorerSystem.getSystems()) {
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