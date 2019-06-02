package net.gunivers.gunibot.command.commands;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map.Entry;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.utils.Util;

public class HelpCommand extends Command
{
	@Override public String getSyntaxFile() { return "help.json"; }

	public void help(MessageCreateEvent event)
	{
		event.getMessage().getChannel().flatMap(channel -> channel.createEmbed(embed -> 
		{
			Util.formatEmbed(event, "**Help Menu**", embed);

			//Field: command list
			StringBuilder sb = new StringBuilder();
			Command.commands.keySet().forEach(aliases -> sb.append(aliases.subList(1, aliases.size() -2).stream()
				.reduce(" - "+ aliases.get(0) + (aliases.size() == 1 ? "" : " ["), (r,s) -> r += s + ", ")
					+ (aliases.size() == 1 ? "" : aliases.get(aliases.size() -1) + "]\n")));

			embed.addField("**Commands List**", sb.toString(), false);
			
			
			//
		})).subscribe();
	}

	public void getHelp(MessageCreateEvent event, List<String> args)
	{
		Command cmd = null;
		for (Entry<List<String>, Command> entry : commands.entrySet())
			if (entry.getKey().contains(args.get(0))) cmd = entry.getValue();
		
		if (cmd == null)
			event.getMessage().getChannel().flatMap(channel ->
			channel.createMessage("```\n❌ There is no command named"+ args.get(0) +"```")).subscribe();
		
		try { cmd.getClass().getMethod("help", MessageCreateEvent.class).invoke(cmd, event); }
		catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			event.getMessage().getChannel().flatMap(channel ->
			channel.createMessage("```\n❌ Unable to retrieve help from command "+ args.get(0) +"```"));
		}
	}
}
