package net.gunivers.gunibot.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.json.JSONObject;

public class SQLClient {

	private static final String SQL_URL = "jdbc:mysql://172.17.228.148?serverTimezone=Europe/Paris"; //IP VM Interne, ne marche pas ailleurs que sur mon PC (Syl2010)
	private static final String SQL_USER = "gunibot";
	private static final String SQL_PASSWORD = "gunibot"; //mot de passe DB VM Interne

	private Connection sqlConnection;
	private boolean isDisable;

	public SQLClient() {
		this(false);
	}

	public SQLClient(boolean optional) {
		isDisable = false;
		try {
			sqlConnection = connect();
			initTables();
		} catch(Exception e) {
			if(optional) {
				System.err.println("[SQLClient] Connection failed : " + e.getMessage());
				System.err.println("[SQLClient] Optional option activated, database disable !");
				isDisable = true;
			} else {
				throw new RuntimeException("Error on database initialization !", e);
			}
		}
	}

	private static Connection connect() {
		try {

			return DriverManager.getConnection(SQL_URL, SQL_USER, SQL_PASSWORD);
		} catch (SQLException e) {
			throw new RuntimeException("Error on database access !", e);
		}
	}

	private void checkConnection() {
		try {
			if(!sqlConnection.isValid(10)) {
				sqlConnection = connect();
			}
		} catch (SQLException e) {
			throw new RuntimeException("The timeout is less than 0 !", e);
		}
	}

	private void initTables() {
		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.useDatabase());
			sqlConnection.createStatement().execute(SQLDataTemplate.createGuildsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createMembersTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createTextChannelsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createRolesTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createVoiceChannelsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createCategoriesTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createUsersTable());

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasGuildData(long guild_id) {
		if(isDisable) return false;
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasGuildData(guild_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeGuildData(long guild_id) {
		if(isDisable) return;
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.removeGuildData(guild_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveGuildData(long guild_id, JSONObject datas) {
		if(isDisable) return;
		checkConnection();

		try {
			for(String line:SQLDataTemplate.insertGuildData(guild_id, datas).split("\n")) sqlConnection.createStatement().execute(line);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public JSONObject loadGuildData(long guild_id) {
		if(isDisable) return new JSONObject();
		checkConnection();

		JSONObject json = new JSONObject();
		JSONObject json_members = new JSONObject();
		JSONObject json_text_channels = new JSONObject();
		JSONObject json_roles = new JSONObject();
		JSONObject json_voice_channels = new JSONObject();
		JSONObject json_categories = new JSONObject();
		try {
			Statement guild_statement = sqlConnection.createStatement();
			guild_statement.execute(SQLDataTemplate.getGuildData(guild_id));
			ResultSet guild_result = guild_statement.getResultSet();
			if(guild_result.next()) {
				json = new JSONObject(guild_result.getString("json"));
			}

			Statement members_statement = sqlConnection.createStatement();
			members_statement.execute(SQLDataTemplate.getMembersDataForGuild(guild_id));
			ResultSet member_result = members_statement.getResultSet();
			while(member_result.next()) {
				long member_id = member_result.getLong("id");
				JSONObject json_member = new JSONObject(member_result.getString("json"));
				json_members.put(String.valueOf(member_id), json_member);
			}

			Statement text_channels_statement = sqlConnection.createStatement();
			text_channels_statement.execute(SQLDataTemplate.getTextChannelsDataForGuild(guild_id));
			ResultSet text_channels_result = text_channels_statement.getResultSet();
			while(text_channels_result.next()) {
				long text_channel_id = text_channels_result.getLong("id");
				JSONObject json_text_channel = new JSONObject(text_channels_result.getString("json"));
				json_text_channels.put(String.valueOf(text_channel_id), json_text_channel);
			}

			Statement roles_statement = sqlConnection.createStatement();
			roles_statement.execute(SQLDataTemplate.getRolesDataForGuild(guild_id));
			ResultSet roles_result = roles_statement.getResultSet();
			while(roles_result.next()) {
				long role_id = roles_result.getLong("id");
				JSONObject json_role = new JSONObject(roles_result.getString("json"));
				json_roles.put(String.valueOf(role_id), json_role);
			}

			Statement voice_channels_statement = sqlConnection.createStatement();
			voice_channels_statement.execute(SQLDataTemplate.getVoiceChannelsDataForGuild(guild_id));
			ResultSet voice_channels_result = voice_channels_statement.getResultSet();
			while(voice_channels_result.next()) {
				long voice_channel_id = voice_channels_result.getLong("id");
				JSONObject json_voice_channel = new JSONObject(voice_channels_result.getString("json"));
				json_voice_channels.put(String.valueOf(voice_channel_id), json_voice_channel);
			}

			Statement categories_statement = sqlConnection.createStatement();
			categories_statement.execute(SQLDataTemplate.getCategoriesDataForGuild(guild_id));
			ResultSet categories_result = categories_statement.getResultSet();
			while(categories_result.next()) {
				long category_id = categories_result.getLong("id");
				JSONObject json_category = new JSONObject(categories_result.getString("json"));
				json_categories.put(String.valueOf(category_id), json_category);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		json.putOpt("members", json_members);
		json.putOpt("text_channels", json_text_channels);
		json.putOpt("roles", json_roles);
		json.putOpt("voice_channels", json_voice_channels);
		json.putOpt("categories", json_categories);
		return json;
	}

	public HashMap<Long,JSONObject> getAllDataGuilds() {
		if(isDisable) return new HashMap<>();
		checkConnection();

		Statement guilds_statement;
		try {
			guilds_statement = sqlConnection.createStatement();
			guilds_statement.execute(SQLDataTemplate.getGuildsId());
			ResultSet guilds_result = guilds_statement.getResultSet();

			HashMap<Long,JSONObject> output = new HashMap<>(guilds_result.getFetchSize());
			while(guilds_result.next()) {
				long guild_id = guilds_result.getLong("id");
				output.put(guild_id, loadGuildData(guild_id));
			}

			return output;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasUserData(long user_id) {
		if(isDisable) return false;
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasUserData(user_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}