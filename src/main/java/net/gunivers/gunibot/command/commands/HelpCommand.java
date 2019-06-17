package net.gunivers.gunibot.command.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.command.permissions.Permission;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.core.command.Command;

public class HelpCommand extends Command
{
	@Override public String getSyntaxFile() { return "help.json"; }

	public void help(MessageCreateEvent event)
	{
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(),
				event.getClient().getSelf().block().getUsername() + "'s Command List", null);
		
		builder.setAuthor(event.getMember().get());
		builder.setAuthor(event.getClient().getSelf().block().asMember(event.getGuildId().get()).block());
		
		builder.setDescription(" - " + commands.keySet().stream().map(l -> l.get(0)).collect(Collectors.joining("\n - ")));
		builder.buildAndSend();
	}

	public void getHelp(MessageCreateEvent event, List<String> args)
	{
		Command cmd = commands.get(commands.keySet().stream().filter(l -> l.contains(args.get(0))).findAny().orElse(new ArrayList<>()));
		if (cmd == null) {
			event.getMessage().getChannel().flatMap(channel -> channel.createMessage("```\n❌ There is no such command as "+ args.get(0) +"!```")).subscribe();
			return;
		}
		
		final EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), "Command: " + args.get(0), null);
		builder.setAuthor(event.getMember().get());
		builder.setAuthor(event.getClient().getSelf().block().asMember(event.getGuildId().get()).block());
		
		final Field info = new Field("General Informations:", "", false);
		info.getValue().append("Aliases: " + cmd.getAliases().stream().collect(Collectors.joining(", ")));
		info.getValue().append("\nSyntax: " + cmd);
		info.getValue().append("\nPermissions: " + cmd.getPermissions().stream().map(Permission::getName).collect(Collectors.joining(", ")));
		
		builder.addField(info);
		builder.addField("Description:", cmd.getDescription(), false);
		builder.addField("Syntax File:", cmd.getSyntaxFile(), false);
		
		builder.buildAndSend();
	}
}
