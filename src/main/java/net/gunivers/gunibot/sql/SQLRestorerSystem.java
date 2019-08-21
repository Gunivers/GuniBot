package net.gunivers.gunibot.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.json.JSONObject;

import net.gunivers.gunibot.core.JsonObjectV2;
import net.gunivers.gunibot.core.main_parser.BotConfig;
import net.gunivers.gunibot.core.system.AbstractRestorerSystem;
import net.gunivers.gunibot.core.system.RestorableSystem;

public class SQLRestorerSystem extends AbstractRestorerSystem {

	public static boolean OPTIONAL = true;

	public static class SQLConfig {
		public final String sqlUrl;
		public final String sqlUser;
		public final String sqlPassword;

		public SQLConfig(BotConfig config) {
			this(config.sql_url, config.sql_user, config.sql_pwd);
		}

		private SQLConfig(String sql_url, String sql_user, String sql_pwd) {
			sqlUrl = sql_url;
			sqlUser = sql_user;
			sqlPassword = sql_pwd;
		}
	}

	private Connection sqlConnection;
	private SQLConfig config;
	private boolean isDisable;

	public SQLRestorerSystem(SQLConfig config) {
		this.config = config;
		try {
			sqlConnection = connect();
			initialize();
		} catch(Exception e) {
			if(OPTIONAL) {
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

	private void initialize() {
		try {
			sqlConnection.createStatement().execute("CREATE TABLE IF NOT EXISTS systems "
					+ "(id varchar(256) NOT NULL PRIMARY KEY, "
					+ "json JSON);");
			sqlConnection.createStatement().execute(SQLDataTemplate.createGuildsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createMembersTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createTextChannelsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createRolesTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createVoiceChannelsTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createCategoriesTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createUsersTable());
			sqlConnection.createStatement().execute(SQLDataTemplate.createOldSerializerTable());
		} catch (SQLException e) {
			throw new RuntimeException("Error on table creation !", e);
		}
	}

	@Override
	public void saveSystem(String id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return;
		}
		checkConnection();

		try {
			sqlConnection.createStatement().execute("REPLACE INTO systems (id,json) VALUE ('"+id+"','"+getSystem(id).save().toString()+"');");
		} catch (SQLException e) {
			throw new RuntimeException("Error on sql writing for system '"+id+"'!", e);
		}
	}

	@Override
	public void loadSystem(String id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return;
		}
		checkConnection();

		try {
			RestorableSystem system = getSystem(id);

			Statement statement = sqlConnection.createStatement();
			statement.execute("SELECT id,json FROM systems WHERE id="+id);
			ResultSet results = statement.getResultSet();

			if(results.next()) {
				system.load(new JSONObject(results.getString("json")));
			} else {
				system.load(new JSONObject());
			}
		} catch (SQLException e) {
			throw new RuntimeException("Error on sql reading for system '"+id+"'!", e);
		}
	}

	public boolean hasGuildData(long guild_id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return false;
		}
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasGuildData(guild_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasUserData(long user_id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return false;
		}
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasUserData(user_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasOldSerializerData(String system_id) {
		if(isDisable) return false;
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasOldSerializerData(system_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean hasSystemData(String system_id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return false;
		}
		checkConnection();

		try {
			return sqlConnection.createStatement().execute(SQLDataTemplate.hasSystemData(system_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeGuildData(long guild_id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return;
		}
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.removeGuildData(guild_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeUserData(long user_id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return;
		}
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.removeUserData(user_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeOldSerializerData(String system_id) {
		if(isDisable) return;
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.removeOldSerializerData(system_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void removeSystemData(String system_id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return;
		}
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.removeSystemData(system_id));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveGuildData(long guild_id, JSONObject datas) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return;
		}
		checkConnection();

		try {
			for(String line:SQLDataTemplate.insertGuildData(guild_id, datas).split("\n")) sqlConnection.createStatement().execute(line);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveUserData(long user_id, JSONObject datas) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return;
		}
		checkConnection();

		try {
			sqlConnection.createStatement().execute(SQLDataTemplate.insertUserData(user_id, datas));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public void saveOldSerializerData(String system_id, JSONObject datas) {
		if(isDisable) return;
		checkConnection();

		try {
			//System.out.println(SQLDataTemplate.insertOldSerializerData(system_id, datas));
			sqlConnection.createStatement().execute(SQLDataTemplate.insertOldSerializerData(system_id, datas));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public JsonObjectV2 loadGuildData(long guild_id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return new JsonObjectV2();
		}
		checkConnection();

		JsonObjectV2 json = new JsonObjectV2();
		JsonObjectV2 json_members = new JsonObjectV2();
		JsonObjectV2 json_text_channels = new JsonObjectV2();
		JsonObjectV2 json_roles = new JsonObjectV2();
		JsonObjectV2 json_voice_channels = new JsonObjectV2();
		JsonObjectV2 json_categories = new JsonObjectV2();
		try {
			Statement guild_statement = sqlConnection.createStatement();
			guild_statement.execute(SQLDataTemplate.getGuildData(guild_id));
			ResultSet guild_result = guild_statement.getResultSet();
			if(guild_result.next()) {
				json = new JsonObjectV2(guild_result.getString("json"));
			}

			Statement members_statement = sqlConnection.createStatement();
			members_statement.execute(SQLDataTemplate.getMembersDataForGuild(guild_id));
			ResultSet member_result = members_statement.getResultSet();
			while(member_result.next()) {
				long member_id = member_result.getLong("id");
				JsonObjectV2 json_member = new JsonObjectV2(member_result.getString("json"));
				json_members.put(String.valueOf(member_id), json_member);
			}

			Statement text_channels_statement = sqlConnection.createStatement();
			text_channels_statement.execute(SQLDataTemplate.getTextChannelsDataForGuild(guild_id));
			ResultSet text_channels_result = text_channels_statement.getResultSet();
			while(text_channels_result.next()) {
				long text_channel_id = text_channels_result.getLong("id");
				JsonObjectV2 json_text_channel = new JsonObjectV2(text_channels_result.getString("json"));
				json_text_channels.put(String.valueOf(text_channel_id), json_text_channel);
			}

			Statement roles_statement = sqlConnection.createStatement();
			roles_statement.execute(SQLDataTemplate.getRolesDataForGuild(guild_id));
			ResultSet roles_result = roles_statement.getResultSet();
			while(roles_result.next()) {
				long role_id = roles_result.getLong("id");
				JsonObjectV2 json_role = new JsonObjectV2(roles_result.getString("json"));
				json_roles.put(String.valueOf(role_id), json_role);
			}

			Statement voice_channels_statement = sqlConnection.createStatement();
			voice_channels_statement.execute(SQLDataTemplate.getVoiceChannelsDataForGuild(guild_id));
			ResultSet voice_channels_result = voice_channels_statement.getResultSet();
			while(voice_channels_result.next()) {
				long voice_channel_id = voice_channels_result.getLong("id");
				JsonObjectV2 json_voice_channel = new JsonObjectV2(voice_channels_result.getString("json"));
				json_voice_channels.put(String.valueOf(voice_channel_id), json_voice_channel);
			}

			Statement categories_statement = sqlConnection.createStatement();
			categories_statement.execute(SQLDataTemplate.getCategoriesDataForGuild(guild_id));
			ResultSet categories_result = categories_statement.getResultSet();
			while(categories_result.next()) {
				long category_id = categories_result.getLong("id");
				JsonObjectV2 json_category = new JsonObjectV2(categories_result.getString("json"));
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

	public JsonObjectV2 loadUserData(long id) {
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return new JsonObjectV2();
		}
		checkConnection();

		try {
			Statement user_statement = sqlConnection.createStatement();
			user_statement.execute(SQLDataTemplate.getUserData(id));
			ResultSet user_result = user_statement.getResultSet();
			if(user_result.next()) {
				return new JsonObjectV2(user_result.getString("json"));
			} else {
				return new JsonObjectV2();
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	public JSONObject loadOldSerializerData(String id) {
		if(isDisable) return new JSONObject();
		checkConnection();

		try {
			Statement system_statement = sqlConnection.createStatement();
			system_statement.execute(SQLDataTemplate.getOldSerializerData(id));
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
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return new HashMap<>();
		}
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
		if(isDisable) {
			System.err.println("SQL system is currently disable!");
			return new HashMap<>();
		}
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

	public HashMap<String,JSONObject> getAllOldDataSerializer() {
		if(isDisable) return new HashMap<>();
		checkConnection();

		try {
			Statement systems_statement = sqlConnection.createStatement();
			systems_statement.execute(SQLDataTemplate.getOldSerializerId());
			ResultSet systems_result = systems_statement.getResultSet();

			HashMap<String, JSONObject> output = new HashMap<>(systems_result.getFetchSize());
			while(systems_result.next()) {
				String system_id = systems_result.getString("id");
				output.put(system_id, loadOldSerializerData(system_id));
			}

			return output;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

}
