package net.gunivers.gunibot.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.json.JSONObject;

import net.gunivers.gunibot.BotConfig;
import net.gunivers.gunibot.core.JsonObjectV2;
import net.gunivers.gunibot.core.system.AbstractRestorerSystem;
import net.gunivers.gunibot.core.system.RestorableSystem;

public class SQLRestorerSystem extends AbstractRestorerSystem {

    public static boolean OPTIONAL = true;

    public static class SQLConfig {
	public final String sqlUrl;
	public final String sqlUser;
	public final String sqlPassword;

	public SQLConfig(BotConfig config) {
	    this(config.sqlUrl, config.sqlUser, config.sqlPassword);
	}

	private SQLConfig(String newSqlUrl, String newSqlUser, String newSqlPwd) {
	    sqlUrl = newSqlUrl;
	    sqlUser = newSqlUser;
	    sqlPassword = newSqlPwd;
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
	} catch (Exception e) {
	    if (OPTIONAL) {
		System.err.println("[SQLClient] Connection failed : " + e.getMessage());
		System.err.println("[SQLClient] Optional option activated, database disabled !");
		isDisable = true;
	    } else
		throw new RuntimeException("Error on database initialization !", e);
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
	    if (!sqlConnection.isValid(10)) {
		sqlConnection = connect();
	    }
	} catch (SQLException e) {
	    throw new RuntimeException("The timeout is less than 0 !", e);
	}
    }

    private void initialize() {
	try {
	    sqlConnection.createStatement().execute(
		    "CREATE TABLE IF NOT EXISTS systems " + "(id varchar(256) NOT NULL PRIMARY KEY, " + "json JSON);");
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
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    sqlConnection.createStatement().execute(
		    "REPLACE INTO systems (id,json) VALUE ('" + id + "','" + getSystem(id).save().toString() + "');");
	} catch (SQLException e) {
	    throw new RuntimeException("Error on sql writing for system '" + id + "'!", e);
	}
    }

    @Override
    public void loadSystem(String id) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    RestorableSystem system = getSystem(id);

	    Statement statement = sqlConnection.createStatement();
	    statement.execute("SELECT id,json FROM systems WHERE id=" + id);
	    ResultSet results = statement.getResultSet();

	    if (results.next()) {
		system.load(new JSONObject(results.getString("json")));
	    } else {
		system.load(new JSONObject());
	    }
	} catch (SQLException e) {
	    throw new RuntimeException("Error on sql reading for system '" + id + "'!", e);
	}
    }

    public boolean hasGuildData(long guildId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return false;
	}
	checkConnection();

	try {
	    return sqlConnection.createStatement().execute(SQLDataTemplate.hasGuildData(guildId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public boolean hasUserData(long userId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return false;
	}
	checkConnection();

	try {
	    return sqlConnection.createStatement().execute(SQLDataTemplate.hasUserData(userId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public boolean hasOldSerializerData(String systemId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return false;
	}
	checkConnection();

	try {
	    return sqlConnection.createStatement().execute(SQLDataTemplate.hasOldSerializerData(systemId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public boolean hasSystemData(String systemId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return false;
	}
	checkConnection();

	try {
	    return sqlConnection.createStatement().execute(SQLDataTemplate.hasSystemData(systemId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void removeGuildData(long guildId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    sqlConnection.createStatement().execute(SQLDataTemplate.removeGuildData(guildId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void removeUserData(long userId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    sqlConnection.createStatement().execute(SQLDataTemplate.removeUserData(userId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void removeOldSerializerData(String systemId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    sqlConnection.createStatement().execute(SQLDataTemplate.removeOldSerializerData(systemId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void removeSystemData(String systemId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    sqlConnection.createStatement().execute(SQLDataTemplate.removeSystemData(systemId));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void saveGuildData(long guildId, JSONObject datas) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    for (String line : SQLDataTemplate.insertGuildData(guildId, datas).split("\n")) {
		sqlConnection.createStatement().execute(line);
	    }
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void saveUserData(long userId, JSONObject datas) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    sqlConnection.createStatement().execute(SQLDataTemplate.insertUserData(userId, datas));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public void saveOldSerializerData(String systemId, JSONObject datas) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return;
	}
	checkConnection();

	try {
	    // System.out.println(SQLDataTemplate.insertOldSerializerData(systemId,
	    // datas));
	    sqlConnection.createStatement().execute(SQLDataTemplate.insertOldSerializerData(systemId, datas));
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public JsonObjectV2 loadGuildData(long guildId) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return new JsonObjectV2();
	}
	checkConnection();

	JsonObjectV2 json = new JsonObjectV2();
	JsonObjectV2 jsonMembers = new JsonObjectV2();
	JsonObjectV2 jsonTextChannels = new JsonObjectV2();
	JsonObjectV2 jsonRoles = new JsonObjectV2();
	JsonObjectV2 jsonVoiceChannels = new JsonObjectV2();
	JsonObjectV2 jsonCategories = new JsonObjectV2();
	try {
	    Statement guildStatement = sqlConnection.createStatement();
	    guildStatement.execute(SQLDataTemplate.getGuildData(guildId));
	    ResultSet guildResult = guildStatement.getResultSet();
	    if (guildResult.next()) {
		json = new JsonObjectV2(guildResult.getString("json"));
	    }

	    Statement membersStatement = sqlConnection.createStatement();
	    membersStatement.execute(SQLDataTemplate.getMembersDataForGuild(guildId));
	    ResultSet memberResult = membersStatement.getResultSet();
	    while (memberResult.next()) {
		long memberId = memberResult.getLong("id");
		JsonObjectV2 jsonMember = new JsonObjectV2(memberResult.getString("json"));
		jsonMembers.put(String.valueOf(memberId), jsonMember);
	    }

	    Statement textChannelsStatement = sqlConnection.createStatement();
	    textChannelsStatement.execute(SQLDataTemplate.getTextChannelsDataForGuild(guildId));
	    ResultSet textChannelsResult = textChannelsStatement.getResultSet();
	    while (textChannelsResult.next()) {
		long textChannelId = textChannelsResult.getLong("id");
		JsonObjectV2 jsonTextChannel = new JsonObjectV2(textChannelsResult.getString("json"));
		jsonTextChannels.put(String.valueOf(textChannelId), jsonTextChannel);
	    }

	    Statement rolesStatement = sqlConnection.createStatement();
	    rolesStatement.execute(SQLDataTemplate.getRolesDataForGuild(guildId));
	    ResultSet rolesResult = rolesStatement.getResultSet();
	    while (rolesResult.next()) {
		long roleId = rolesResult.getLong("id");
		JsonObjectV2 jsonRole = new JsonObjectV2(rolesResult.getString("json"));
		jsonRoles.put(String.valueOf(roleId), jsonRole);
	    }

	    Statement voiceChannelsStatement = sqlConnection.createStatement();
	    voiceChannelsStatement.execute(SQLDataTemplate.getVoiceChannelsDataForGuild(guildId));
	    ResultSet voiceChannelsResult = voiceChannelsStatement.getResultSet();
	    while (voiceChannelsResult.next()) {
		long voiceChannelId = voiceChannelsResult.getLong("id");
		JsonObjectV2 jsonVoiceChannel = new JsonObjectV2(voiceChannelsResult.getString("json"));
		jsonVoiceChannels.put(String.valueOf(voiceChannelId), jsonVoiceChannel);
	    }

	    Statement categoriesStatement = sqlConnection.createStatement();
	    categoriesStatement.execute(SQLDataTemplate.getCategoriesDataForGuild(guildId));
	    ResultSet categoriesResult = categoriesStatement.getResultSet();
	    while (categoriesResult.next()) {
		long categoryId = categoriesResult.getLong("id");
		JsonObjectV2 jsonCategory = new JsonObjectV2(categoriesResult.getString("json"));
		jsonCategories.put(String.valueOf(categoryId), jsonCategory);
	    }
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}

	json.putOpt("members", jsonMembers);
	json.putOpt("text_channels", jsonTextChannels);
	json.putOpt("roles", jsonRoles);
	json.putOpt("voice_channels", jsonVoiceChannels);
	json.putOpt("categories", jsonCategories);

	return json;
    }

    public JsonObjectV2 loadUserData(long id) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return new JsonObjectV2();
	}
	checkConnection();

	try {
	    Statement userStatement = sqlConnection.createStatement();
	    userStatement.execute(SQLDataTemplate.getUserData(id));
	    ResultSet userResult = userStatement.getResultSet();
	    if (userResult.next())
		return new JsonObjectV2(userResult.getString("json"));
	    else
		return new JsonObjectV2();
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public JSONObject loadOldSerializerData(String id) {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return new JsonObjectV2();
	}
	checkConnection();

	try {
	    Statement systemStatement = sqlConnection.createStatement();
	    systemStatement.execute(SQLDataTemplate.getOldSerializerData(id));
	    ResultSet systemResult = systemStatement.getResultSet();
	    if (systemResult.next())
		return new JSONObject(systemResult.getString("json"));
	    else
		return new JSONObject();
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public HashMap<Long, JSONObject> getAllDataGuilds() {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return new HashMap<>();
	}
	checkConnection();

	try {
	    Statement guildsStatement = sqlConnection.createStatement();
	    guildsStatement.execute(SQLDataTemplate.getGuildsId());
	    ResultSet guildsResult = guildsStatement.getResultSet();

	    HashMap<Long, JSONObject> output = new HashMap<>(guildsResult.getFetchSize());
	    while (guildsResult.next()) {
		long guildId = guildsResult.getLong("id");
		output.put(guildId, loadGuildData(guildId));
	    }

	    return output;
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public HashMap<Long, JSONObject> getAllDataUsers() {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return new HashMap<>();
	}
	checkConnection();

	try {
	    Statement usersStatement = sqlConnection.createStatement();
	    usersStatement.execute(SQLDataTemplate.getUsersId());
	    ResultSet usersResult = usersStatement.getResultSet();

	    HashMap<Long, JSONObject> output = new HashMap<>(usersResult.getFetchSize());
	    while (usersResult.next()) {
		long userId = usersResult.getLong("id");
		output.put(userId, loadUserData(userId));
	    }

	    return output;
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

    public HashMap<String, JSONObject> getAllOldDataSerializer() {
	if (isDisable) {
	    System.err.println("SQL system is currently disabled!");
	    return new HashMap<>();
	}
	checkConnection();

	try {
	    Statement systemsStatement = sqlConnection.createStatement();
	    systemsStatement.execute(SQLDataTemplate.getOldSerializerId());
	    ResultSet systemsResult = systemsStatement.getResultSet();

	    HashMap<String, JSONObject> output = new HashMap<>(systemsResult.getFetchSize());
	    while (systemsResult.next()) {
		String systemId = systemsResult.getString("id");
		output.put(systemId, loadOldSerializerData(systemId));
	    }

	    return output;
	} catch (SQLException e) {
	    throw new RuntimeException(e);
	}
    }

}
