package net.gunivers.gunibot.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.json.JSONObject;

import net.gunivers.gunibot.core.main_parser.BotConfig;

public class SQLClient {

	public static class SQLConfig {
		public final String sqlUrl;
		public final String sqlUser;
		public final String sqlPassword;
		public final String sqlDb;

		public SQLConfig(BotConfig config) {
			this(config.sql_url, config.sql_user, config.sql_pwd, config.sql_db);
		}

		private SQLConfig(String sql_url, String sql_user, String sql_pwd, String sql_db) {
			sqlUrl = sql_url;
			sqlUser = sql_user;
			sqlPassword = sql_pwd;
			sqlDb = sql_db;
		}
	}

	//public static final SQLConfig TEST_CONFIG = new SQLConfig(String.format(BotConfig.SQL_URL_FORMAT, "localhost"), "gunibot", "gunibot", "gunibot");

	private Connection sqlConnection;
	private boolean isDisable;
	private SQLConfig config;

	public SQLClient(SQLConfig config) {
		this(config, false);
	}

	public SQLClient(SQLConfig config, boolean optional) {
		this.config = config;
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

	private Connection connect() {
		try {
			return DriverManager.getConnection(config.sqlUrl, config.sqlUser, config.sqlPassword);
		} catch (SQLException e) {
			throw new RuntimeException(String.format("Error on database access!\nURL = %s", config.sqlUrl), e);
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
			sqlConnection.createStatement().execute(SQLDataTemplate.useDatabase(config.sqlDb));
			sqlConnection.createStatement().execute(SQLDataTemplate.createGuildsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createMembersTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createTextChannelsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createRolesTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createVoiceChannelsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createCategoriesTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createUsersTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createSystemsTable());

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

	public boolean hasUserData(long user_id) {
		if(isDisable) return false;
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasUserData(user_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasSystemData(String system_id) {
		if(isDisable) return false;
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasSystemData(system_id));
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

	public void removeUserData(long user_id) {
		if(isDisable) return;
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.removeUserData(user_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeSystemData(String system_id) {
		if(isDisable) return;
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.removeSystemData(system_id));
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

	public void saveUserData(long user_id, JSONObject datas) {
		if(isDisable) return;
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.insertUserData(user_id, datas));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveSystemData(String system_id, JSONObject datas) {
		if(isDisable) return;
		checkConnection();

		try {
			//System.out.println(SQLDataTemplate.insertSystemData(system_id, datas));
			sqlConnection.createStatement().execute(SQLDataTemplate.insertSystemData(system_id, datas));
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

	public JSONObject loadUserData(long id) {
		if(isDisable) return new JSONObject();
		checkConnection();

		try {
			Statement user_statement = sqlConnection.createStatement();
			user_statement.execute(SQLDataTemplate.getUserData(id));
			ResultSet user_result = user_statement.getResultSet();
			if(user_result.next()) {
				return new JSONObject(user_result.getString("json"));
			} else {
				return new JSONObject();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public JSONObject loadSystemData(String id) {
		if(isDisable) return new JSONObject();
		checkConnection();

		try {
			Statement system_statement = sqlConnection.createStatement();
			system_statement.execute(SQLDataTemplate.getSystemData(id));
			ResultSet system_result = system_statement.getResultSet();
			if(system_result.next()) {
				return new JSONObject(system_result.getString("json"));
			} else {
				return new JSONObject();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public HashMap<Long,JSONObject> getAllDataGuilds() {
		if(isDisable) return new HashMap<>();
		checkConnection();

		try {
			Statement guilds_statement = sqlConnection.createStatement();
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

	public HashMap<Long,JSONObject> getAllDataUsers() {
		if(isDisable) return new HashMap<>();
		checkConnection();

		try {
			Statement users_statement = sqlConnection.createStatement();
			users_statement.execute(SQLDataTemplate.getUsersId());
			ResultSet users_result = users_statement.getResultSet();

			HashMap<Long,JSONObject> output = new HashMap<>(users_result.getFetchSize());
			while(users_result.next()) {
				long user_id = users_result.getLong("id");
				output.put(user_id, loadUserData(user_id));
			}

			return output;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public HashMap<String,JSONObject> getAllDataSystems() {
		if(isDisable) return new HashMap<>();
		checkConnection();

		try {
			Statement systems_statement = sqlConnection.createStatement();
			systems_statement.execute(SQLDataTemplate.getSystemsId());
			ResultSet systems_result = systems_statement.getResultSet();

			HashMap<String, JSONObject> output = new HashMap<>(systems_result.getFetchSize());
			while(systems_result.next()) {
				String system_id = systems_result.getString("id");
				output.put(system_id, loadSystemData(system_id));
			}

			return output;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}