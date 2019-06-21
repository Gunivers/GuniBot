package net.gunivers.gunibot.command.commands.configuration;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.core.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.datas.Configuration;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataObject;

public class ConfigCommand extends Command {

	@Override
	public String getSyntaxFile() { return "configuration/config.json"; }

	public void list(MessageCreateEvent event)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());

		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for server " + g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());


		Field names = new Field("Name"); Field types = new Field("Type");
		builder.addField(names); builder.addField(types);

		for (Configuration<?> config : Configuration.all)
		{
			names.getValue().append(config.getName() + '\n');
			types.getValue().append(config.getValueType().getSimpleName() + '\n');
		}

		builder.buildAndSend();
	}

	public void get(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), g.getEntity().getName() + "'s Configuration", null);
		builder.setRequestedBy(event.getMember().get());

		Configuration.all.stream().filter(c -> c.getName().equalsIgnoreCase(args.get(0))).forEach(config ->
		builder.setDescription(config.getName() +":\t"
				+ (config.get(g) == null ? "NO_VALUE" : config.get(g).toString())
				+ (builder.getDescription() == null ? "" : builder.getDescription())));

		if (builder.getDescription() == null)
			builder.setDescription("404: No configuration found for name: "+ args.get(0));

		builder.buildAndSend();
	}

	public void set(MessageCreateEvent event, List<String> args)
	{

		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());

		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), g.getEntity().getName() + "'s Configuration", null);
		builder.setRequestedBy(event.getMember().get());

		Configuration.all.stream().filter(c -> c.getName().equalsIgnoreCase(args.get(0))).forEach(c -> {
			try
			{
				builder.setDescription(c.getName());

				builder.addField("Old value", String.valueOf(c.get(g)), true);
				builder.addField("New Value", args.get(1), true);

				c.set(g, args.get(1));
			} catch (Exception e)
			{
				builder.clear();
				builder.setDescription("An error occured while parsing! " + e.getClass().getSimpleName() +": "+ e.getMessage()
				+ "\nConfiguration for '"+ c.getName() +"' should be of type '"+ c.getValueType().getSimpleName().substring(4) +'\'');

				e.printStackTrace();
			}
		});

		if (builder.getDescription() == null)
			builder.setDescription("404: No configuration found for name: "+ args.get(0));

		builder.buildAndSend();
	}

	public Class<?> getParameterizedType(DataObject<?> object)
	{
		try
		{
			return ConfigCommand.class.getMethod("getParameterizedType", DataObject.class).getParameters()[0].getParameterizedType().getClass();
		} catch (NoSuchMethodException | SecurityException e) { e.printStackTrace(); }

		return null;
	}
}
