package net.gunivers.gunibot.core.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;

public class CommandIssuedListener extends Events<MessageCreateEvent> {
    private final HashMap<DataGuild, ArrayList<Command>> history = new HashMap<>();
    private Tuple2<DataGuild, Command> last = null;

    protected CommandIssuedListener() {
	super(MessageCreateEvent.class);
    }

    @Override
    protected boolean precondition(MessageCreateEvent event) {
	if (!event.getMember().isPresent())
	    return false;
	DataGuild dataGuild = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());

	java.util.Optional<String> message = event.getMessage().getContent();
	if (!message.isPresent() || !message.get().startsWith(dataGuild.getPrefix()))
	    return false;

	String name = message.get().split(" ")[0].substring(dataGuild.getPrefix().length());
	Optional<List<String>> optList = Command.commands.keySet().stream().filter(list -> list.contains(name))
		.findAny();
	if (!optList.isPresent())
	    return false;

	System.out.println(
		event.getMember().get().getDisplayName() + " issued command: " + event.getMessage().getContent().get());

	if (!history.containsKey(dataGuild)) {
	    history.put(dataGuild, new ArrayList<>());
	}
	last = Tuple.newTuple(dataGuild, Command.commands.get(optList.get()));
	history.get(dataGuild).add(last.value2);

	if (!Permission.hasPermissions(event.getMember().get(), this.getLastCommand().value2.getPermissions())) {
	    event.getMessage().getChannel().flatMap(c -> c.createMessage("Insufficient permissions!")).subscribe();
	    return false;
	}

	return true;
    }

    @Override
    protected void apply(MessageCreateEvent event) {
	this.onCommand(event.getMessage().getContent().get(), last.value1, last.value2);
    }

    public Map<DataGuild, List<Command>> getHistory() {
	return Collections.unmodifiableMap(history);
    }

    public Tuple2<DataGuild, Command> getLastCommand() {
	return last == null ? null : Tuple.newTuple(last.value1, last.value2);
    }

    public void clearHistory() {
	history.clear();
    }

    public void onCommand(String arguments, DataGuild dataGuild, Command command) {
	Matcher matcher = Pattern.compile(" (\"(.[^\"]|\\\")*\")|.[^ ]+")
		.matcher(arguments.substring(dataGuild.getPrefix().length()));

	List<String> args = new ArrayList<>();
	while (matcher.find()) {
	    args.add(matcher.group());
	}

	args = args.stream().map(String::trim)
		.map(string -> string.charAt(0) == '"' ? string.substring(1, string.length() - 1) : string)
		.collect(Collectors.toList());
	command.apply(args.stream().collect(Collectors.joining(" ")), super.last);
    }
}
