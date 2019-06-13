package net.gunivers.gunibot.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.core.command.Command;

public class CommandIssuedListener extends Events<MessageCreateEvent>
{
	private final ArrayList<Command> history = new ArrayList<>();
	
	protected CommandIssuedListener() { super(MessageCreateEvent.class); }

	@Override
	protected boolean precondition(MessageCreateEvent event)
	{
		java.util.Optional<String> msg = event.getMessage().getContent();
		if (!msg.isPresent() || !msg.get().startsWith(Command.PREFIX)) return false;
		
		String name = msg.get().split(" ")[0].substring(Command.PREFIX.length());
		Optional<List<String>> get = Command.commands.keySet().stream().filter(l -> l.contains(name)).findAny();
		if (!get.isPresent()) return false;

		System.out.println(event.getMember().get().getDisplayName() + " issued command: " + event.getMessage().getContent().get());
		
		history.add(Command.commands.get(get.get()));
		if (!Permission.hasPermissions(event.getMember().get(), this.getLastCommand().getPermissions())) {
			event.getMessage().getChannel().flatMap(c -> c.createMessage("Insufficient permissions!")).subscribe();
			return false;
		}
		
		return true;
	}

	@Override
	protected void apply(MessageCreateEvent event)
	{
		this.onCommand(event.getMessage().getContent().get(), this.getLastCommand());
	}
	
	public List<Command> getHistory() { return Collections.unmodifiableList(history); }
	public void clearHistory() { history.clear(); }
	
	public Command getLastCommand() { return history.get(history.size() -1); }
	
	public void onCommand(String arguments, Command cmd)
	{
		Matcher m = Pattern.compile(" (\"(.[^\"]|\\\")*\")|.[^ ]+").matcher(arguments.substring(Command.PREFIX.length()));
		
		List<String> args = new ArrayList<>();
		while (m.find())
			args.add(m.group());
		
		args = args.stream().map(String::trim).map(s -> s.charAt(0) == '"' ? s.substring(1, s.length() -1) : s).collect(Collectors.toList());
		this.getLastCommand().apply(args.stream().collect(Collectors.joining(" ")), last);
	}
}
