package net.gunivers.gunibot.core.main_parser;

import java.io.File;
import java.io.IOException;

public class BotConfig {

	public static final String SQL_URL_FORMAT = "jdbc:mysql://%s?serverTimezone=Europe/Paris&autoReconnect=true&failOverReadOnly=false&maxReconnects=3";

	public final String token;
	public final String sql_url;
	public final String sql_user;
	public final String sql_pwd;
	public final String sql_db;

	public BotConfig(ArgumentParser arg_parser) {
		File conf_file = new File(arg_parser.getDefaultArguments("f", "./config"));
		ConfigParser config;
		try {
			config = new ConfigParser(conf_file);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		token = arg_parser.getDefaultArguments("t", config.getDefaultArguments("token", ""));
		if(token.isEmpty()) throw new IllegalArgumentException("Aucun token n'as été donné !");

		if (arg_parser.hasArguments("sql_url") && arg_parser.hasArguments("sql_ip")) throw new IllegalArgumentException("");
		else if (config.hasArguments("sql_url") && config.hasArguments("sql_ip")) throw new IllegalArgumentException("");
		else if (arg_parser.hasArguments("sql_url")) sql_url = arg_parser.getArguments("sql_url");
		else if (arg_parser.hasArguments("sql_ip")) sql_url = String.format(SQL_URL_FORMAT, arg_parser.getArguments("sql_ip"));
		else if (config.hasArguments("sql_url")) sql_url = config.getArguments("sql_url");
		else sql_url = String.format(SQL_URL_FORMAT, config.getDefaultArguments("sql_ip", "localhost"));


		sql_user = arg_parser.getDefaultArguments("sql_user", config.getDefaultArguments("sql_user", "gunibot"));
		sql_pwd = arg_parser.getDefaultArguments("sql_pwd", config.getDefaultArguments("sql_password", ""));
		if(sql_pwd.isEmpty()) System.err.println("Aucun mot de passe précisé pour la base de données ! La base de données ne fonctionnera pas sans un mot de passe !");
		sql_db = arg_parser.getDefaultArguments("sql_db", config.getDefaultArguments("sql_database", "gunibot"));
	}

}
