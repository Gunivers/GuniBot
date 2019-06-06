package net.gunivers.gunibot.event;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.permissions.Permission;

public class CommandIssuedListener extends Events<MessageCreateEvent>
{
	private Command cmd;
	
	protected CommandIssuedListener() { super(MessageCreateEvent.class); }

	@Override
	protected boolean precondition(MessageCreateEvent event)
	{
		java.util.Optional<String> msg = event.getMessage().getContent();
		if (!msg.isPresent() || !msg.get().startsWith(Command.PREFIX)) return false;
		
		String name = msg.get().split(" ")[0];
		Optional<List<String>> get = Command.commands.keySet().stream().filter(l -> l.contains(name)).findAny();
		if (!get.isPresent()) return false;
		
		cmd = Command.commands.get(get.get());
		if (!Permission.hasPermissions(event.getMember().get(), cmd.getPermissions())) {
			event.getMessage().getChannel().flatMap(c -> c.createMessage("Insufficient permissions!"));
			return false;
		}
		
		return true;
	}

	@Override
	protected void apply(MessageCreateEvent event)
	{
		this.onCommand(event.getMessage().getContent().get());
	}
	
	public Command getLastCommand() { return cmd; }
	
	public void onCommand(String command)
	{
		Matcher m = Pattern.compile(" (\"?(.[^\"]|\\\")*\")|.[^ ]+").matcher(command);
		
		List<String> args = new ArrayList<>();
		for (int i = 1; i <= m.groupCount(); i++)
			args.add(m.group(i));
		
		args = args.stream().map(s -> s.charAt(0) == '"' ? s.substring(1, s.length() -2) : s).collect(Collectors.toList());
		cmd.apply(args.toArray(new String[0]), last);
	}
}
