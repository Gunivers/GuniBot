package net.gunivers.gunibot.command.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map.Entry;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.az.lib.EmbedBuilder;
import net.gunivers.gunibot.command.lib.Command;

public class HelpCommand extends Command
{
	@Override public String getSyntaxFile() { return "help.json"; }

	public void help(MessageCreateEvent event)
	{
		EmbedBuilder builder = new EmbedBuilder(event, "Help Menu", null);
		builder.setAuthor(event.getMember().get());
	}

	public void getHelp(MessageCreateEvent event, List<String> args)
	{
		Command cmd = null;
		for (Entry<List<String>, Command> entry : commands.entrySet()) if (entry.getKey().contains(args.get(0))) cmd = entry.getValue();
		
		if (cmd == null)
		{
			event.getMessage().getChannel().flatMap(channel ->
				channel.createMessage("```\n❌ There is no such command as "+ args.get(0) +"!```")).subscribe();
			return;
		}
		
		try { cmd.getClass().getMethod("help", MessageCreateEvent.class).invoke(cmd, event); }
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			event.getMessage().getChannel().flatMap(channel ->
				channel.createMessage("```\n❌ Unable to retrieve help from command "+ args.get(0) +"```")).subscribe();
		}
	}
}
