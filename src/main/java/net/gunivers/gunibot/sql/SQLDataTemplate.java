package net.gunivers.gunibot.sql;

import java.util.HashSet;

import org.json.JSONObject;

public final class SQLDataTemplate {

	// Create Tables

	public static String createGuildsTable() {
		return "CREATE TABLE IF NOT EXISTS guilds "
				+ "(id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createMembersTable() {
		return "CREATE TABLE IF NOT EXISTS members "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds (id) ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE, "
				+ "json JSON);";
	}

	public static String createTextChannelsTable() {
		return "CREATE TABLE IF NOT EXISTS text_channels "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds (id) ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createRolesTable() {
		return "CREATE TABLE IF NOT EXISTS roles "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds (id) ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createVoiceChannelsTable() {
		return "CREATE TABLE IF NOT EXISTS voice_channels "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds (id) ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createCategoriesTable() {
		return "CREATE TABLE IF NOT EXISTS categories "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds (id) ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createUsersTable() {
		return "CREATE TABLE IF NOT EXISTS users "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds (id) ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String useDatabase() {
		return "USE gunibot;";
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

	// has Datas

	public static String hasGuildData(long id) {
		return "SELECT id FROM guilds WHERE id="+id+" LIMIT 1;";
	}

	public static String hasUserData(long id) {
		return "SELECT id FROM users WHERE id="+id+" LIMIT 1;";
	}

	// get Datas

	public static String getGuildData(long id) {
		return "SELECT id,json FROM guilds WHERE id="+id+";";
	}

	public static String getMembersDataForGuild(long id) {
		return "SELECT guilds.id,members.id,members.json FROM guilds,members WHERE guilds.id="+id+";";
	}

	public static String getTextChannelsDataForGuild(long id) {
		return "SELECT guilds.id,text_channels.id,text_channels.json FROM guilds,text_channels WHERE guilds.id="+id+";";
	}

	public static String getRolesDataForGuild(long id) {
		return "SELECT guilds.id,roles.id,roles.json FROM guilds,roles WHERE guilds.id="+id+";";
	}

	public static String getVoiceChannelsDataForGuild(long id) {
		return "SELECT guilds.id,voice_channels.id,voice_channels.json FROM guilds,voice_channels WHERE guilds.id="+id+";";
	}

	public static String getCategoriesDataForGuild(long id) {
		return "SELECT guilds.id,categories.id,categories.json FROM guilds,categories WHERE guilds.id="+id+";";
	}

	public static String getUserData(long id) {
		return "SELECT id,json FROM users WHERE id="+id;
	}

	public static String getGuildsId() {
		return "SELECT id FROM guilds;";
	}

	public static String getUsersId() {
		return "SELECT is FROM users;";
	}

	public static String getGuildsData() {
		return "SELECT id,json FROM guilds;";
	}

	public static String getUsersData() {
		return "SELECT is,json FROM users;";
	}

	// remove Datas

	public static String removeGuildData(long id) {
		return "DELETE FROM guilds WHERE id="+id+";";
	}

	public static String removeUserData(long id) {
		return "DELETE FROM users WHERE id="+id+";";
	}

	// insert Datas

	public static String insertGuildData(long id, JSONObject json) {
		String sql = "REPLACE INTO guilds id VALUE "+id+";";

		JSONObject json_members = json.optJSONObject("members");
		if(json_members != null) {
			HashSet<String> entries = new HashSet<>();

			for(String member_id:json_members.keySet()) {
				JSONObject json_member = json_members.getJSONObject(member_id);
				entries.add("("+id+","+member_id+","+json_member+")");
			}
			sql += "REPLACE INTO members (guild_id,id,json) VALUES (" + String.join(",", entries) + ");";
			json.remove("members");
		}

		JSONObject json_text_channels = json.optJSONObject("text_channels");
		if(json_text_channels != null) {
			HashSet<String> entries = new HashSet<>();

			for(String text_channel_id:json_text_channels.keySet()) {
				JSONObject json_text_channel = json_text_channels.getJSONObject(text_channel_id);
				entries.add("("+id+","+text_channel_id+","+json_text_channel+")");
			}
			sql += "REPLACE INTO text_channels (guild_id,id,json) VALUES (" + String.join(",", entries) + ");";
			json.remove("text_channels");
		}

		JSONObject json_roles = json.optJSONObject("roles");
		if(json_roles != null) {
			HashSet<String> entries = new HashSet<>();

			for(String role_id:json_roles.keySet()) {
				JSONObject json_role = json_roles.getJSONObject(role_id);
				entries.add("("+id+","+role_id+","+json_role+")");
			}
			sql += "REPLACE INTO roles (guild_id,id,json) VALUES (" + String.join(",", entries) + ");";
			json.remove("roles");
		}

		JSONObject json_voice_channels = json.optJSONObject("voice_channels");
		if(json_text_channels != null) {
			HashSet<String> entries = new HashSet<>();

			for(String voice_channel_id:json_voice_channels.keySet()) {
				JSONObject json_voice_channel = json_voice_channels.getJSONObject(voice_channel_id);
				entries.add("("+id+","+voice_channel_id+","+json_voice_channel+")");
			}
			sql += "REPLACE INTO voice_channels (guild_id,id,json) VALUES (" + String.join(",", entries) + ");";
			json.remove("voice_channels");
		}

		JSONObject json_categories = json.optJSONObject("categories");
		if(json_categories != null) {
			HashSet<String> entries = new HashSet<>();

			for(String category_id:json_categories.keySet()) {
				JSONObject json_category = json_categories.getJSONObject(category_id);
				entries.add("("+id+","+category_id+","+json_category+")");
			}
			sql += "REPLACE INTO categories (guild_id,id,json) VALUES (" + String.join(",", entries) + ");";
			json.remove("categories");
		}

		sql += "REPLACE INTO guilds (guild_id,json) VALUE ("+id+","+json+");";

		return sql;
	}

}
