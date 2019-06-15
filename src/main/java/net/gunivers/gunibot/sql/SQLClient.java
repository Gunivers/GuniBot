package net.gunivers.gunibot.sql;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.json.JSONObject;

public class SQLClient {

	private static final String SQL_URL = "jdbc:mysql://172.18.85.253?serverTimezone=Europe/Paris"; //IP VM Interne, ne marche pas ailleurs que sur mon PC (Syl2010)
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
			PreparedStatement statement = sqlConnection.prepareStatement(SQLDataTemplate.getGuildData(guild_id));
			statement.execute();
			ResultSet result = statement.getResultSet();
			while(result.next()) {
				BigDecimal member_id = result.getBigDecimal("members.id");
				JSONObject json_member = result.getObject("members.json", JSONObject.class);
				if(member_id != null) json_members.put(member_id.toPlainString(), json_member);

				BigDecimal text_channel_id = result.getBigDecimal("text_channels.id");
				JSONObject json_text_channel = result.getObject("text_channels.json", JSONObject.class);
				if(text_channel_id != null) json_text_channels.put(text_channel_id.toPlainString(), json_text_channel);

				BigDecimal role_id = result.getBigDecimal("roles.id");
				JSONObject json_role = result.getObject("roles.json", JSONObject.class);
				if(role_id != null) json_roles.put(role_id.toPlainString(), json_role);

				BigDecimal voice_channel_id = result.getBigDecimal("voice_channels.id");
				JSONObject json_voice_channel = result.getObject("voice_channels.json", JSONObject.class);
				if(voice_channel_id != null) json_voice_channels.put(voice_channel_id.toPlainString(), json_voice_channel);

				BigDecimal category_id = result.getBigDecimal("categories.id");
				JSONObject json_category = result.getObject("categories.json", JSONObject.class);
				if(category_id != null) json_categories.put(category_id.toPlainString(), json_category);

				result.getObject("json");
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


		// TODO get All Data guilds from SQL Database
		return new HashMap<>();
	}

	public boolean hasUserData(long user_id) {
		if(isDisable) return false;
		checkConnection();


		// TODO Check SQL Database for User
		return false;
	}

}