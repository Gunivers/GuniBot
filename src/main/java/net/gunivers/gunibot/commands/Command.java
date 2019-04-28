package net.gunivers.gunibot.commands;

import java.util.HashSet;
import java.util.Set;

import org.reflections.Reflections;

public abstract class Command {
	
	public static final String PREFIX = "/";
	public static final Set<Command> commands = new HashSet<>();

	public Command() {
	}
	
	/**
	 * Permet de charger toutes les commandes du Package net.gunivers.gunibot.commands
	 */
	public static void loadCommands() {
		Reflections reflections = new Reflections("net.gunivers.gunibot.commands");
		 Set<Class<? extends Command>> allCommands = reflections.getSubTypesOf(Command.class);
		 allCommands.forEach(cmd -> {
			try {
				if(!cmd.isAnnotationPresent(Ignore.class))
					commands.add(cmd.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}

}
