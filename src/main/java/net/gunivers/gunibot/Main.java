package net.gunivers.gunibot;

import fr.syl2010.utils.io.parser.UnixCommandLineParser;
import net.gunivers.gunibot.core.BotConfig;
import net.gunivers.gunibot.core.BotInstance;

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