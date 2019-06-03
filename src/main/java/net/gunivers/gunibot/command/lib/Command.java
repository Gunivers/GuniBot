package net.gunivers.gunibot.command.lib;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.utils.tuple.Tuple2;

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

	public Set<String> getPermissions() {
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
	
	public void apply(String[] command, MessageCreateEvent event)
	{
		Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> result = syntax.matches(command);
		if(result._1 != null)
		{
			try
			{
				if(result._1._1.size() > 0)
				result._1._2.invoke(this, event, result._1._1);
				else
					result._1._2.invoke(this, event);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e)
			{
				e.printStackTrace();
			}
		} else
		{
			List<String> list = result._2.getPath();
			list.set(list.size() - 1, "__" + list.get(list.size() - 1) + "__");
			event.getMessage().getChannel().block().createMessage("Une erreur a été détectée ici : " + list.stream().collect(Collectors.joining(" ")) + '\n' + result._2).subscribe();
//			System.out.println("Une erreur a été détectée ici : " + result._2.getPath().stream().collect(Collectors.joining(" ")));
		}
	}

	public abstract String getSyntaxFile();
	
	/**
	 * Permet de charger toutes les commandes du Package net.gunivers.gunibot.command.commands
	 */
	public static void loadCommands() {
		Reflections reflections = new Reflections("net.gunivers.gunibot.command.commands");
		 Set<Class<? extends Command>> allCommands = reflections.getSubTypesOf(Command.class);
		 allCommands.forEach(cmd -> {
			try {
				if(!cmd.isAnnotationPresent(Ignore.class)) {
					Command c = cmd.newInstance();
					NodeRoot n = (NodeRoot)CommandParser.parseCommand(c);
					List<String> aliases = n.getAliases();
//					Function.functions.put(aliases.get(0), n);
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
