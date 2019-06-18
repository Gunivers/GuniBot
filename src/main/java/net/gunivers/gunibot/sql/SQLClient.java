package net.gunivers.gunibot.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONObject;

public class SQLClient {

	private static final String SQL_URL = "jdbc:mysql://172.17.228.156?serverTimezone=Europe/Paris"; //IP VM Interne, ne marche pas ailleurs que sur mon PC (Syl2010)
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
			sqlConnection.prepareStatement(SQLDataTemplate.useDatabase()).execute();
			sqlConnection.prepareStatement(SQLDataTemplate.createGuildsTable()).execute();
			sqlConnection.prepareStatement(SQLDataTemplate.createMembersTable()).execute();
			sqlConnection.prepareStatement(SQLDataTemplate.createTextChannelsTable()).execute();
			sqlConnection.prepareStatement(SQLDataTemplate.createRolesTable()).execute();
			sqlConnection.prepareStatement(SQLDataTemplate.createVoiceChannelsTable()).execute();
			sqlConnection.prepareStatement(SQLDataTemplate.createCategoriesTable()).execute();
			sqlConnection.prepareStatement(SQLDataTemplate.createUsersTable()).execute();

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasGuildData(long guild_id) {
		if(isDisable) return false;
		checkConnection();

		try {
			return sqlConnection.prepareStatement(SQLDataTemplate.hasGuildData(guild_id)).execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeGuildData(long guild_id) {
		if(isDisable) return;
		checkConnection();

		try {
			sqlConnection.prepareStatement(SQLDataTemplate.removeGuildData(guild_id)).execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveGuildData(long guild_id, JSONObject datas) {
		if(isDisable) return;
		checkConnection();

		try {
			sqlConnection.prepareStatement(SQLDataTemplate.insertGuildData(guild_id, datas)).execute();
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
			PreparedStatement guild_statement = sqlConnection.prepareStatement(SQLDataTemplate.getGuildData(guild_id));
			guild_statement.execute();
			ResultSet guild_result = guild_statement.getResultSet();
			if(guild_result.next()) {
				json = new JSONObject(guild_result.getObject("json", JSONObject.class));
			}

			PreparedStatement members_statement = sqlConnection.prepareStatement(SQLDataTemplate.getMembersDataForGuild(guild_id));
			members_statement.execute();
			ResultSet member_result = members_statement.getResultSet();
			while(member_result.next()) {
				long member_id = member_result.getLong("members.id");
				JSONObject json_member = member_result.getObject("members.json", JSONObject.class);
				json_members.put(String.valueOf(member_id), json_member);
			}

			PreparedStatement text_channels_statement = sqlConnection.prepareStatement(SQLDataTemplate.getTextChannelsDataForGuild(guild_id));
			text_channels_statement.execute();
			ResultSet text_channels_result = text_channels_statement.getResultSet();
			while(text_channels_result.next()) {
				long text_channel_id = text_channels_result.getLong("text_channels.id");
				JSONObject json_text_channel = text_channels_result.getObject("text_channels.json", JSONObject.class);
				json_text_channels.put(String.valueOf(text_channel_id), json_text_channel);
			}

			PreparedStatement roles_statement = sqlConnection.prepareStatement(SQLDataTemplate.getRolesDataForGuild(guild_id));
			roles_statement.execute();
			ResultSet roles_result = roles_statement.getResultSet();
			while(roles_result.next()) {
				long role_id = roles_result.getLong("roles.id");
				JSONObject json_role = roles_result.getObject("roles.json", JSONObject.class);
				json_roles.put(String.valueOf(role_id), json_role);
			}

			PreparedStatement voice_channels_statement = sqlConnection.prepareStatement(SQLDataTemplate.getVoiceChannelsDataForGuild(guild_id));
			voice_channels_statement.execute();
			ResultSet voice_channels_result = voice_channels_statement.getResultSet();
			while(voice_channels_result.next()) {
				long voice_channel_id = voice_channels_result.getLong("voice_channels.id");
				JSONObject json_voice_channel = voice_channels_result.getObject("voice_channels.json", JSONObject.class);
				json_voice_channels.put(String.valueOf(voice_channel_id), json_voice_channel);
			}

			PreparedStatement categories_statement = sqlConnection.prepareStatement(SQLDataTemplate.getCategoriesDataForGuild(guild_id));
			categories_statement.execute();
			ResultSet categories_result = categories_statement.getResultSet();
			while(categories_result.next()) {
				long category_id = categories_result.getLong("categories.id");
				JSONObject json_category = categories_result.getObject("categories.json", JSONObject.class);
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

		PreparedStatement guilds_statement;
		try {
			guilds_statement = sqlConnection.prepareStatement(SQLDataTemplate.getGuildsId());
			guilds_statement.execute();
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
			return sqlConnection.prepareStatement(SQLDataTemplate.hasUserData(user_id)).execute();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}