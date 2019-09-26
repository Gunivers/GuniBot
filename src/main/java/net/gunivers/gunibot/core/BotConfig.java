package net.gunivers.gunibot.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import discord4j.core.object.util.Snowflake;
import fr.syl2010.utils.io.parser.UnixCommandLineParser;
import fr.syl2010.utils.io.parser.UnixConfigParser;

public class BotConfig {

    public static final String SQL_URL_FORMAT = "jdbc:mysql://%s/%s?serverTimezone=Europe/Paris&autoReconnect=true&failOverReadOnly=false&maxReconnects=3";

    public final String token;
    public final String sqlUrl;
    public final String sqlUser;
    public final String sqlPassword;
    public final List<Snowflake> developperIds;

    public BotConfig(UnixCommandLineParser argParser) {
	File confFile = new File(argParser.getDefaultArguments("f", "./config"));
	UnixConfigParser config;
	try {
	    config = new UnixConfigParser(confFile);
	} catch (IOException e) {
	    throw new RuntimeException(e);
	}
	developperIds = new ArrayList<>();

	token = argParser.getDefaultArguments("t", config.getDefaultArguments("token", ""));
	if (token.isEmpty())
	    throw new IllegalArgumentException("Aucun token n'as été donné !");

	String sqlDatabase = argParser.getDefaultArguments("sql_db",
		config.getDefaultArguments("sql_database", "gunibot"));

	if (argParser.hasArguments("sql_url") && argParser.hasArguments("sql_ip"))
	    throw new IllegalArgumentException("");
	else if (config.hasArguments("sql_url") && config.hasArguments("sql_ip"))
	    throw new IllegalArgumentException("");
	else if (argParser.hasArguments("sql_url")) {
	    sqlUrl = argParser.getArguments("sql_url");
	} else if (argParser.hasArguments("sql_ip")) {
	    sqlUrl = String.format(SQL_URL_FORMAT, argParser.getArguments("sql_ip"), sqlDatabase);
	} else if (config.hasArguments("sql_url")) {
	    sqlUrl = config.getArguments("sql_url");
	} else {
	    sqlUrl = String.format(SQL_URL_FORMAT, config.getDefaultArguments("sql_ip", "localhost"), sqlDatabase);
	}

	sqlUser = argParser.getDefaultArguments("sql_user", config.getDefaultArguments("sql_user", "gunibot"));
	sqlPassword = argParser.getDefaultArguments("sql_pwd", config.getDefaultArguments("sql_password", ""));
	if (sqlPassword.isEmpty()) {
	    System.err.println(
		    "Aucun mot de passe précisé pour la base de données ! La base de données ne fonctionnera pas sans un mot de passe !");
	}

	String strDevelopperIds = config.getDefaultArguments("developpers_ids", "");
	if (!strDevelopperIds.isEmpty()) {
	    developperIds.addAll(Arrays.asList(strDevelopperIds.split(",")).stream().map(Snowflake::of)
		    .collect(Collectors.toList()));
	}
	strDevelopperIds = argParser.getDefaultArguments("dev_ids", "");
	if (!strDevelopperIds.isEmpty()) {
	    developperIds.addAll(Arrays.asList(strDevelopperIds.split(",")).stream().map(Snowflake::of)
		    .collect(Collectors.toList()));
	}
    }

}
