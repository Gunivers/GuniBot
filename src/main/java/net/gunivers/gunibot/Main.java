package net.gunivers.gunibot;

import net.gunivers.gunibot.core.main_parser.ArgumentParser;
import net.gunivers.gunibot.core.main_parser.BotConfig;

public class Main {

	private static BotInstance botInstance;

	public static void main(String[] args) {
		botInstance = new BotInstance(new BotConfig(new ArgumentParser(args)));
		botInstance.loginBlock(); // lance le bot en bloquant le thread principal
	}

	public static BotInstance getBotInstance() {
		return botInstance;
	}

}