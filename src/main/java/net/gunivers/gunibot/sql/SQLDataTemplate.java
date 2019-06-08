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
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds id ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY REFERENCES users id ON DELETE CASCADE, "
				+ "json JSON);";
	}

	public static String createTextChannelsTable() {
		return "CREATE TABLE IF NOT EXISTS text_channels "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds id ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createRolesTable() {
		return "CREATE TABLE IF NOT EXISTS roles "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds id ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createVoiceChannelsTable() {
		return "CREATE TABLE IF NOT EXISTS voice_channels "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds id ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createCategoriesTable() {
		return "CREATE TABLE IF NOT EXISTS categories "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds id ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String createUsersTable() {
		return "CREATE TABLE IF NOT EXISTS users "
				+ "(guild_id BIGINT NOT NULL UNIQUE KEY REFERENCES guilds id ON DELETE CASCADE, "
				+ "id BIGINT NOT NULL PRIMARY KEY, "
				+ "json JSON);";
	}

	public static String useDatabase() {
		return "USE gunibot;";
	}

	// Check Tables

	public static String checkGuildsTable() {
		return "SELECT id FROM guilds LIMIT 1;";
	}

	public static String checkMembersTable() {
		return "SELECT id FROM members LIMIT 1;";
	}

	public static String checkTextChannelsTable() {
		return "SELECT id FROM text_channels LIMIT 1;";
	}

	public static String checkRolesTable() {
		return "SELECT id FROM roles LIMIT 1;";
	}

	public static String checkVoiceChannelsTable() {
		return "SELECT id FROM voice_channels LIMIT 1;";
	}

	public static String checkCategoriesTable() {
		return "SELECT id FROM categories LIMIT 1;";
	}

	public static String checkUsersTable() {
		return "SELECT id FROM users LIMIT 1;";
	}

	// has Datas

	public static String hasGuildData(long id) {
		return "SELECT id FROM guilds LIMIT 1 WHERE id="+id+";";
	}

	public static String hasUserData(long id) {
		return "SELECT id FROM users LIMIT 1 WHERE id="+id+";";
	}

	// get Datas

	public static String getGuildData(long id) {
		return "SELECT * FROM guilds WHERE id="+id+" "
				+ "UNION SELECT * FROM members "
				+ "UNION SELECT * FROM text_channels "
				+ "UNION SELECT * FROM roles "
				+ "UNION SELECT * FROM voice_channels "
				+ "UNION SELECT * FROM categories;";
	}

	public static String getUserData(long id) {
		return "SELECT * FROM users WHERE id="+id;
	}

	public static String getGuildsData() {
		return "SELECT * FROM guilds "
				+ "UNION SELECT * FROM members "
				+ "UNION SELECT * FROM text_channels "
				+ "UNION SELECT * FROM roles "
				+ "UNION SELECT * FROM voice_channels "
				+ "UNION SELECT * FROM categories;";
	}

	public static String getUsersData() {
		return "SELECT * FROM users;";
	}

	// remove Datas

	public static String removeGuildData(long id) {
		return "SELECT * FROM guilds WHERE id="+id+" "
				+ "UNION SELECT * FROM members "
				+ "UNION SELECT * FROM text_channels "
				+ "UNION SELECT * FROM roles "
				+ "UNION SELECT * FROM voice_channels "
				+ "UNION SELECT * FROM categories;";
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
