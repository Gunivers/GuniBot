package net.gunivers.gunibot.commands.lib;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

public abstract class Command {
	
	public static final String PREFIX = "/";
	public static final Map<List<String>, Command> commands = new HashMap<>();
	
	private String description = "";
	private Node syntax = null;
	private Set<String> permissions = new HashSet<>();
	private Set<String> aliases = new HashSet<>();

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Set<String> getPersmissions() {
		return permissions;
	}

	public void addPermissions(List<String> permission) {
		permissions.addAll(permission);
	}

	public Set<String> getAliases() {
		return aliases;
	}

	public void setAliases(String alias) {
		aliases.add(alias);
	}
	
	public void setSyntax(Node n) {
		syntax = n;
	}

	public abstract String getSyntaxFile();
	
	/**
	 * Permet de charger toutes les commandes du Package net.gunivers.gunibot.commands
	 */
	public static void loadCommands() {
		Reflections reflections = new Reflections("net.gunivers.gunibot.commands");
		 Set<Class<? extends Command>> allCommands = reflections.getSubTypesOf(Command.class);
		 allCommands.forEach(cmd -> {
			try {
				if(!cmd.isAnnotationPresent(Ignore.class)) {
					Command c = cmd.newInstance();
					List<String> aliases = CommandSyntaxParser.createTree(c);
					commands.put(aliases, c);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}
	
	public static void main(String... args) {
		loadCommands();
	}
}
