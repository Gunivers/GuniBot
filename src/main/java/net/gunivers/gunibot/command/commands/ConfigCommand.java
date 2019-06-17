package net.gunivers.gunibot.command.commands;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.datas.Configuration;
import net.gunivers.gunibot.datas.DataGuild;

public class ConfigCommand extends Command {

	@Override
	public String getSyntaxFile() { return "config.json"; }

	public void list(MessageCreateEvent event)
	{	
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), "Configuration for server " + g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());
		
		Field names = new Field("Name"); Field types = new Field("Type"); Field values = new Field("Value");
		for (Configuration<?> config : Configuration.all)
		{
			names.getValue().append(config.getName() + '\n');
			types.getValue().append(config.getClass().getSimpleName() + '\n');
			values.getValue().append(config.get(g).toString());
		}
		
		builder.addField(names); builder.addField(types); builder.addField(values);
		builder.buildAndSend();
	}
	
	public void get(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), g.getEntity().getName() + "'s Configuration", null);
		builder.setRequestedBy(event.getMember().get());
		
		Configuration.all.stream().filter(c -> c.getName().equalsIgnoreCase(args.get(0))).forEach(config ->
			builder.setDescription(config.getName() +":\t"+ config.get(g).toString()));

		if (builder.getDescription() == null)
			builder.setDescription("404: No configuration found for name: "+ args.get(0));
		
		builder.buildAndSend();
	}

	public void set(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), g.getEntity().getName() + "'s Configuration", null);
		builder.setRequestedBy(event.getMember().get());
		
		Configuration.all.stream().filter(c -> c.getName().equalsIgnoreCase(args.get(0))).forEach(c -> {
			try
			{
				builder.setDescription(c.getName());
				builder.addField("Old Value", c.get(g).toString(), true);
				builder.addField("New Value", args.get(1), true);

				c.set(g, args.get(1));
			} catch (Exception e)
			{
				builder.clear();
				builder.setDescription("Error while parsing. Configuration for '"+ c.getName() +"' should be of type '"+ c.getClass().getSimpleName() +'\'');
			}
		});
		
		if (builder.getDescription() == null)
			builder.setDescription("404: No configuration found for name: "+ args.get(0));
		
		builder.buildAndSend();
	}
}
