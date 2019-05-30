package net.gunivers.gunibot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.json.JSONObject;

public class SQLClient {

	private static final String SQL_URL = "";
	private static final String SQL_USER = "";
	private static final String SQL_PASSWORD = "";

	private Connection sqlConnection;

	public SQLClient() {
		sqlConnection = connect();
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

	public boolean hasGuildData(long id) {
		checkConnection();


		// TODO Check SQL Database for Guild
		return false;
	}

	public void saveGuildData(long id, JSONObject datas) {
		checkConnection();


		// TODO Save Data of Guild to the SQL Database
	}

	public void removeGuildData(long asLong) {
		checkConnection();


		// TODO Remove Data of Guild from the SQL Database
	}

	public boolean hasUserData(long id) {
		checkConnection();


		// TODO Check SQL Database for User
		return false;
	}

}
