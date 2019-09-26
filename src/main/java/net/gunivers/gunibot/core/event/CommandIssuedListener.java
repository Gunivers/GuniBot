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
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public class CommandIssuedListener extends Events<MessageCreateEvent>
{
	private final HashMap<DataGuild, ArrayList<Command>> history = new HashMap<>();
	private Tuple2<DataGuild, Command> last = null;
	
	protected CommandIssuedListener() { super(MessageCreateEvent.class); }

	@Override
	protected boolean precondition(MessageCreateEvent event)
	{
		if (!event.getMember().isPresent()) return false;
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		
		java.util.Optional<String> msg = event.getMessage().getContent();
		if (!msg.isPresent() || !msg.get().startsWith(g.getPrefix())) return false;
		
		String name = msg.get().split(" ")[0].substring(g.getPrefix().length());
		Optional<List<String>> get = Command.commands.keySet().stream().filter(l -> l.contains(name)).findAny();
		if (!get.isPresent()) return false;

		System.out.println(event.getMember().get().getDisplayName() + " issued command: " + event.getMessage().getContent().get());
		
		if (!history.containsKey(g)) history.put(g, new ArrayList<>());
		last = Tuple.newTuple(g, Command.commands.get(get.get()));
		history.get(g).add(last._2);

		if (!Permission.hasPermissions(event.getMember().get(), this.getLastCommand()._2.getPermissions())) {
			event.getMessage().getChannel().flatMap(c -> c.createMessage("Insufficient permissions!")).subscribe();
			return false;
		}
		
		return true;
	}

	@Override
	protected void apply(MessageCreateEvent event)
	{
		this.onCommand(event.getMessage().getContent().get(), last._1, last._2);
	}
	
	public Map<DataGuild, List<Command>> getHistory() { return Collections.unmodifiableMap(history); }
	public Tuple2<DataGuild, Command> getLastCommand() { return last == null ? null : Tuple.newTuple(last._1, last._2); }
	
	public void clearHistory() { history.clear(); }
	
	public void onCommand(String arguments, DataGuild g, Command cmd)
	{
		Matcher m = Pattern.compile(" (\"(.[^\"]|\\\")*\")|.[^ ]+").matcher(arguments.substring(g.getPrefix().length()));
		
		List<String> args = new ArrayList<>();
		while (m.find())
			args.add(m.group());
		
		args = args.stream().map(String::trim).map(s -> s.charAt(0) == '"' ? s.substring(1, s.length() -1) : s).collect(Collectors.toList());
		cmd.apply(args.stream().collect(Collectors.joining(" ")), super.last);
	}
}
