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
			Util.formatEmbed(event, "Help Menu", embed);

			//Field: command list
			StringBuilder sb = new StringBuilder();
			for (List<String> aliases : Command.commands.keySet())
			{
				for (String alias : aliases)
				{
					if (alias == aliases.get(0)) sb.append(" - " + alias + (aliases.size() > 1 ? " [" : ""));
					else sb.append(alias);
					
					if (aliases.size() > 1 && alias == aliases.get(aliases.size() -1)) sb.append("]\n");
					else if (alias != aliases.get(0)) sb.append(", ");
				}
			}

			embed.addField("Commands List", sb.toString(), false);
		})).subscribe();
	}

	public void getHelp(MessageCreateEvent event, List<String> args)
	{
		Command cmd = null;
		for (Entry<List<String>, Command> entry : commands.entrySet()) if (entry.getKey().contains(args.get(0))) cmd = entry.getValue();
		
		if (cmd == null)
		{
			event.getMessage().getChannel().flatMap(channel ->
				channel.createMessage("```\n❌ There is no such command as "+ args.get(0) +"```")).subscribe();
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
