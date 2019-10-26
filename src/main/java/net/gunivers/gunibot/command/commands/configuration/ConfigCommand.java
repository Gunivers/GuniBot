package net.gunivers.gunibot.command.commands.configuration;

import java.util.List;
import java.util.Map.Entry;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.datas.config.Configuration;
import net.gunivers.gunibot.core.datas.config.ConfigurationNode;
import net.gunivers.gunibot.core.datas.config.ConfigurationTree;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.core.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.core.lib.parsing.ParsingException;

import discord4j.core.event.domain.message.MessageCreateEvent;

public class ConfigCommand extends Command
{
	private static final String CONFIG_CHANGE_FAILED = "FAILURE";

	public void list(MessageCreateEvent event)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());

		Field names = new Field("Configuration Trees");
		builder.addField(names);

		for (Entry<String, ConfigurationTree> config : g.getConfiguration().asMap().entrySet())
			if (config.getValue().isVisible())
				names.getValue().append(config.getKey() + '\n');

		builder.buildAndSend();
	}

	public void listNode(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());

		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		if (node == null || !node.isVisible())
		{
			builder.setDescription("Node '"+ args.get(0) +"' not found!");
			builder.buildAndSend();
			return;
		}

		builder.setDescription('`'+ args.get(0) +'`');
		Field name = new Field("Name");
		Field type = new Field("Type");
		Field nature = new Field("Nature");

		for (Entry<String, ConfigurationNode> child : node.getChildren().entrySet()) if (child.getValue().isVisible())
		{
			name.getValue().append(child.getKey() +'\n');
			if (child.getValue().isConfiguration())
			{
				type.getValue().append(child.getValue().asConfiguration().getType() +'\n');
				nature.getValue().append("CONFIGURATION\n");
			} else
			{
				type.getValue().append('\n');
				nature.getValue().append("NODE\n");
			}
		}

		builder.addField(name);
		builder.addField(type);
		builder.addField(nature);
		builder.buildAndSend();
	}

	public void get(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());

		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		if (node == null || !node.isVisible())
		{
			builder.setDescription("There is no node nor configuration at path '"+ args.get(0) +"'");
			builder.buildAndSend();
			return;
		}

		builder.addField("Name", node.getName(), true);

		if (node.isConfiguration())
		{
			Configuration<?> config = (Configuration<?>) node;
			builder.setDescription("CONFIGURATION");
			builder.addField("Value", config.getValue().toString(), true);
			builder.addField("Type", config.getType(), true);
		} else
			builder.setDescription("NODE");

		builder.addField("Path", node.getPath(), true);
		builder.buildAndSend();
	}

	public void set(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());

		Field output = new Field("Output", false);
		builder.addField(output);

		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		if (node == null || !node.isVisible())
		{
			output.getValue().append(CONFIG_CHANGE_FAILED);
			builder.addField("Reason", "Configuration '"+ args.get(0) +"' not found!", false);
		}
		else if (node.isConfiguration())
			try
			{
				Configuration<?> config = node.asConfiguration();
				Field old = new Field("Old Value", String.valueOf(config.getValue()));

				config.set(args.get(1));
				output.getValue().append("SUCCESS");

				builder.addField(old);
				builder.addField("New Value", String.valueOf(config.getValue()), true);
			} catch (ParsingException e)
			{
				output.getValue().append(CONFIG_CHANGE_FAILED);
				builder.addField("Reason", e.getMessage(), false);
			}
		else
		{
			output.getValue().append(CONFIG_CHANGE_FAILED);
			builder.addField("Reason", "The node '"+ args.get(0) +"' isn't a configuration!", false);
		}

		builder.buildAndSend();
	}

	public void reset(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());

		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		if (node == null || !node.isVisible())
			builder.setDescription("Configuration '"+ args.get(0) +"' not found!");
		else if (node.isConfiguration())
		{
			Configuration<?> config = node.asConfiguration();
			builder.setDescription('\''+ args.get(0) +"' got successfully reseted!");

			builder.addField("Old Value", String.valueOf(config.getValue()), true);
			node.asConfiguration().reset();
			builder.addField("New Value", String.valueOf(config.getValue()), true);
		}
		else
			builder.setDescription("The node '"+ args.get(0) + "' isn't a configuration");

		builder.buildAndSend();
	}

	@Override
	public String getSyntaxFile() { return "configuration/config.json"; }
}
