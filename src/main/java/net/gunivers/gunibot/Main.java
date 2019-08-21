package net.gunivers.gunibot;

import fr.syl2010.utils.io.parser.UnixCommandLineParser;

public class Main {

	private static BotInstance botInstance;

	public static void main(String[] args) {
		botInstance = new BotInstance(new BotConfig(new UnixCommandLineParser(args)));
		botInstance.loginBlock(); // lance le bot en bloquant le thread principal
	}

	public static BotInstance getBotInstance() {
		return botInstance;
	}

}