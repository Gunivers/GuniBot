package net.gunivers.gunibot.core.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;

import discord4j.core.DiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.core.command.nodes.Node;
import net.gunivers.gunibot.core.command.nodes.NodeRoot;
import net.gunivers.gunibot.core.datas.DataCenter;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;

public abstract class Command {

    public static final Map<List<String>, Command> commands = new HashMap<>();

    private String description = "";
    private Node syntax = null;
    private Set<Permission> permissions = new HashSet<>();
    private Set<String> aliases = new HashSet<>();
    private Map<String, Node> idReference = new HashMap<>();
    protected static DataCenter dataCenter;
    protected static DiscordClient discordClient;

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    public Set<Permission> getPermissions() {
	return permissions;
    }

    public void addPermissions(List<String> permissions) {
	for (String perm : permissions) {
	    Set<Permission> perms = Permission.getByName(perm);
	    if (perms.isEmpty())
		throw new NullPointerException("Permission '" + perm + "' doesn't exist");
	    this.permissions.addAll(perms);
	}
    }

    public Set<String> getAliases() {
	return aliases;
    }

    public void setAliases(String alias) {
	aliases.add(alias);
    }

    public void setSyntax(Node node) {
	syntax = node;
    }

    public void addIdReference(String string, Node node) {
	idReference.put(string, node);
    }

    public Optional<Node> getNodeById(String string) {
	return Optional.ofNullable(idReference.get(string));
    }

    public Set<String> getReferences() {
	return idReference.keySet();
    }

    public boolean isReferenced(String string) {
	return idReference.containsKey(string);
    }

    public void apply(String command, MessageCreateEvent event) {
	Tuple2<Tuple2<List<String>, Method>, CommandSyntaxError> result = syntax.matches(command, this);
	if (result.value1 != null) {
	    try {
		if (result.value1.value1.size() > 0) {
		    result.value1.value2.invoke(this, event, result.value1.value1);
		} else {
		    result.value1.value2.invoke(this, event);
		}
	    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
		e.printStackTrace();
	    }
	} else {
	    List<String> list = result.value2.getPath();
	    list.set(list.size() - 1, "__" + list.get(list.size() - 1) + "__");
	    event.getMessage().getChannel().block().createMessage("Une erreur a été détectée ici : "
		    + list.stream().collect(Collectors.joining(" ")) + '\n' + result.value2).subscribe();
	    // System.out.println("Une erreur a été détectée ici : " +
	    // result.value2.getPath().stream().collect(Collectors.joining(" ")));
	}
    }

    public abstract String getSyntaxFile();

    /**
     * Permet de charger toutes les commandes du Package
     * net.gunivers.gunibot.command.commands
     */
    public static void loadCommands(DataCenter dataCenter, DiscordClient discordClient) {
	Command.dataCenter = dataCenter;
	Command.discordClient = discordClient;
	Reflections reflections = new Reflections("net.gunivers.gunibot.command.commands");
	Set<Class<? extends Command>> allCommands = reflections.getSubTypesOf(Command.class);
	allCommands.forEach(commandClass -> {
	    try {
		if (!commandClass.isAnnotationPresent(Ignore.class)) {
		    Command command = commandClass.newInstance();
		    NodeRoot nodeRoot = CommandParser.parseCommand(command);
		    if (nodeRoot != null) {
			System.out.println(nodeRoot.getElements().get(0));
			List<String> aliases = nodeRoot.getElements();
			commands.put(aliases, command);
		    }
		}
	    } catch (InstantiationException | IllegalAccessException e) {
		e.printStackTrace();
	    }
	});
    }

    @Override
    public String toString() {
	return syntax.toString();
    }
}