package net.gunivers.gunibot.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.json.JSONObject;

import net.gunivers.gunibot.core.system.AbstractRestorerSystem;
import net.gunivers.gunibot.core.system.RestorableSystem;



public class SQLRestorerSystem extends AbstractRestorerSystem {

	public static class SQLConfig {
		public final String sqlUrl;
		public final String sqlUser;
		public final String sqlPassword;

		private SQLConfig(String sql_url, String sql_user, String sql_pwd) {
			sqlUrl = sql_url;
			sqlUser = sql_user;
			sqlPassword = sql_pwd;
		}
	}

	private Connection sqlConnection;
	private SQLConfig config;

	public SQLRestorerSystem(SQLConfig config) {
		this.config = config;
		try {
			sqlConnection = connect();
			initialize();
		} catch(Exception e) {
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
		} catch (SQLException e) {
			throw new RuntimeException("Error on table creation !", e);
		}
	}

	@Override
	public void saveSystem(String id) {
		checkConnection();

		try {
			sqlConnection.createStatement().execute("REPLACE INTO systems (id,json) VALUE ('"+id+"','"+getSystem(id).save().toString()+"');");
		} catch (SQLException e) {
			throw new RuntimeException("Error on sql writing for system '"+id+"'!", e);
		}
	}

	@Override
	public void loadSystem(String id) {
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

	public static String formatJDBCUrl(String ip, String db) {
		return String.format("jdbc:mysql://%s/%s?serverTimezone=Europe/Paris", ip, db);
	}

}
