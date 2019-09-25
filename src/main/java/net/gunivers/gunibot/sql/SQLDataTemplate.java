package net.gunivers.gunibot.sql;

import java.util.HashSet;

import org.json.JSONObject;

public final class SQLDataTemplate {

    // Create Tables

    public static String createGuildsTable() {
	return "CREATE TABLE IF NOT EXISTS guilds " + "(id BIGINT NOT NULL PRIMARY KEY, " + "json JSON);";
    }

    public static String createMembersTable() {
	return "CREATE TABLE IF NOT EXISTS members "
		+ "(guild_id BIGINT NOT NULL REFERENCES guilds (id) ON DELETE CASCADE, "
		+ "id BIGINT NOT NULL PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE, " + "json JSON);";
    }

    public static String createTextChannelsTable() {
	return "CREATE TABLE IF NOT EXISTS text_channels "
		+ "(guild_id BIGINT NOT NULL REFERENCES guilds (id) ON DELETE CASCADE, "
		+ "id BIGINT NOT NULL PRIMARY KEY, " + "json JSON);";
    }

    public static String createRolesTable() {
	return "CREATE TABLE IF NOT EXISTS roles "
		+ "(guild_id BIGINT NOT NULL REFERENCES guilds (id) ON DELETE CASCADE, "
		+ "id BIGINT NOT NULL PRIMARY KEY, " + "json JSON);";
    }

    public static String createVoiceChannelsTable() {
	return "CREATE TABLE IF NOT EXISTS voice_channels "
		+ "(guild_id BIGINT NOT NULL REFERENCES guilds (id) ON DELETE CASCADE, "
		+ "id BIGINT NOT NULL PRIMARY KEY, " + "json JSON);";
    }

    public static String createCategoriesTable() {
	return "CREATE TABLE IF NOT EXISTS categories "
		+ "(guild_id BIGINT NOT NULL REFERENCES guilds (id) ON DELETE CASCADE, "
		+ "id BIGINT NOT NULL PRIMARY KEY, " + "json JSON);";
    }

    public static String createUsersTable() {
	return "CREATE TABLE IF NOT EXISTS users " + "(id BIGINT NOT NULL PRIMARY KEY, " + "json JSON);";
    }

    public static String createOldSerializerTable() {
	return "CREATE TABLE IF NOT EXISTS serializer " + "(id VARCHAR(256) NOT NULL PRIMARY KEY, " + "json JSON);";
    }

    // Check Tables

    public static String checkGuildsTable() {
	return "SHOW TABLES LIKE 'guilds';";
    }

    public static String checkMembersTable() {
	return "SHOW TABLES LIKE 'members';";
    }

    public static String checkTextChannelsTable() {
	return "SHOW TABLES LIKE 'text_channels';";
    }

    public static String checkRolesTable() {
	return "SHOW TABLES LIKE 'roles';";
    }

    public static String checkVoiceChannelsTable() {
	return "SHOW TABLES LIKE 'voice_channels';";
    }

    public static String checkCategoriesTable() {
	return "SHOW TABLES LIKE 'categories';";
    }

    public static String checkUsersTable() {
	return "SHOW TABLES LIKE 'users';";
    }

    public static String checkSystemsTable() {
	return "SHOW TABLES LIKE 'systems';";
    }

    // has Datas

    public static String hasGuildData(long id) {
	return "SELECT id FROM guilds WHERE id=" + id + " LIMIT 1;";
    }

    public static String hasUserData(long id) {
	return "SELECT id FROM users WHERE id=" + id + " LIMIT 1;";
    }

    public static String hasOldSerializerData(String id) {
	return "SELECT id FROM serializer WHERE id='" + id + "' LIMIT 1;";
    }

    public static String hasSystemData(String id) {
	return "SELECT id FROM systems WHERE id='" + id + "' LIMIT 1;";
    }

    // get Datas

    public static String getGuildData(long id) {
	return "SELECT id,json FROM guilds WHERE id=" + id + ";";
    }

    public static String getMembersDataForGuild(long id) {
	return "SELECT members.guild_id,members.id,members.json FROM guilds,members WHERE guilds.id=" + id + ";";
    }

    public static String getTextChannelsDataForGuild(long id) {
	return "SELECT text_channels.guild_id,text_channels.id,text_channels.json FROM guilds,text_channels WHERE guilds.id="
		+ id + ";";
    }

    public static String getRolesDataForGuild(long id) {
	return "SELECT roles.guild_id,roles.id,roles.json FROM guilds,roles WHERE guilds.id=" + id + ";";
    }

    public static String getVoiceChannelsDataForGuild(long id) {
	return "SELECT voice_channels.guild_id,voice_channels.id,voice_channels.json FROM guilds,voice_channels WHERE guilds.id="
		+ id + ";";
    }

    public static String getCategoriesDataForGuild(long id) {
	return "SELECT categories.guild_id,categories.id,categories.json FROM guilds,categories WHERE guilds.id=" + id
		+ ";";
    }

    public static String getUserData(long id) {
	return "SELECT id,json FROM users WHERE id=" + id;
    }

    public static String getOldSerializerData(String id) {
	return "SELECT id,json FROM serializer WHERE id='" + id + "'";
    }

    public static String getGuildsId() {
	return "SELECT id FROM guilds;";
    }

    public static String getUsersId() {
	return "SELECT id FROM users;";
    }

    public static String getOldSerializerId() {
	return "SELECT id FROM serializer;";
    }

    public static String getSystemsId() {
	return "SELECT id FROM systems;";
    }

    public static String getGuildsData() {
	return "SELECT id,json FROM guilds;";
    }

    public static String getUsersData() {
	return "SELECT id,json FROM users;";
    }

    public static String getOldSerializerData() {
	return "SELECT id,json FROM serializer;";
    }

    // remove Datas

    public static String removeGuildData(long id) {
	return "DELETE FROM guilds WHERE id=" + id + ";";
    }

    public static String removeUserData(long id) {
	return "DELETE FROM users WHERE id=" + id + ";";
    }

    public static String removeOldSerializerData(String id) {
	return "DELETE FROM serializer WHERE id=" + id + ";";
    }

    public static String removeSystemData(String id) {
	return "DELETE FROM systems WHERE id=" + id + ";";
    }

    // insert Datas

    public static String insertGuildData(long id, JSONObject json) {
	String sql = "REPLACE INTO guilds (id) VALUE (" + id + ");\n";

	JSONObject jsonMembers = json.optJSONObject("members");
	if (jsonMembers != null) {
	    HashSet<String> entries = new HashSet<>();

	    if (jsonMembers.length() > 1) {
		for (String memberId : jsonMembers.keySet()) {
		    JSONObject jsonMember = jsonMembers.getJSONObject(memberId);
		    entries.add("(" + id + "," + memberId + ",'" + jsonMember.toString() + "')");
		}
		sql += "REPLACE INTO members (guild_id,id,json) VALUES " + String.join(",", entries) + ";\n";
	    }
	    json.remove("members");
	}

	JSONObject jsonTextChannels = json.optJSONObject("text_channels");
	if (jsonTextChannels != null) {
	    HashSet<String> entries = new HashSet<>();

	    if (jsonTextChannels.length() > 1) {
		for (String textChannelId : jsonTextChannels.keySet()) {
		    JSONObject jsonTextChannel = jsonTextChannels.getJSONObject(textChannelId);
		    entries.add("(" + id + "," + textChannelId + ",'" + jsonTextChannel.toString() + "')");
		}
		sql += "REPLACE INTO text_channels (guild_id,id,json) VALUES " + String.join(",", entries) + ";\n";
	    }
	    json.remove("text_channels");
	}

	JSONObject jsonRoles = json.optJSONObject("roles");
	if (jsonRoles != null) {
	    HashSet<String> entries = new HashSet<>();

	    if (jsonRoles.length() > 1) {
		for (String roleId : jsonRoles.keySet()) {
		    JSONObject jsonRole = jsonRoles.getJSONObject(roleId);
		    entries.add("(" + id + "," + roleId + ",'" + jsonRole.toString() + "')");
		}
		sql += "REPLACE INTO roles (guild_id,id,json) VALUES " + String.join(",", entries) + ";\n";
	    }
	    json.remove("roles");
	}

	JSONObject jsonVoiceChannels = json.optJSONObject("voice_channels");
	if (jsonTextChannels != null) {
	    HashSet<String> entries = new HashSet<>();

	    if (jsonTextChannels.length() > 1) {
		for (String voiceChannelId : jsonVoiceChannels.keySet()) {
		    JSONObject jsonVoiceChannel = jsonVoiceChannels.getJSONObject(voiceChannelId);
		    entries.add("(" + id + "," + voiceChannelId + ",'" + jsonVoiceChannel.toString() + "')");
		}
		sql += "REPLACE INTO voice_channels (guild_id,id,json) VALUES " + String.join(",", entries) + ";\n";
	    }
	    json.remove("voice_channels");
	}

	JSONObject jsonCategories = json.optJSONObject("categories");
	if (jsonCategories != null) {
	    HashSet<String> entries = new HashSet<>();

	    if (jsonCategories.length() > 1) {
		for (String categoryId : jsonCategories.keySet()) {
		    JSONObject jsonCategory = jsonCategories.getJSONObject(categoryId);
		    entries.add("(" + id + "," + categoryId + ",'" + jsonCategory.toString() + "')");
		}
		sql += "REPLACE INTO categories (guild_id,id,json) VALUES " + String.join(",", entries) + ";\n";
	    }
	    json.remove("categories");
	}

	sql += "REPLACE INTO guilds (id,json) VALUE (" + id + ",'" + json.toString() + "');";

	return sql;
    }

    public static String insertUserData(long user_id, JSONObject datas) {
	return "REPLACE INTO users (id,json) VALUE (" + user_id + ",'" + datas.toString() + "');";
    }

    public static String insertOldSerializerData(String system_id, JSONObject datas) {
	return "REPLACE INTO serializer (id,json) VALUE ('" + system_id + "','" + datas.toString() + "');";
    }

    public static String insertSystemData(String system_id, JSONObject datas) {
	return "REPLACE INTO systems (id,json) VALUE ('" + system_id + "','" + datas.toString() + "');";
    }

}
