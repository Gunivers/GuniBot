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
	private static final String CONFIG_CHANGE_FAILED = "Failed to set configuration :c";

	public void list(MessageCreateEvent event)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());

		Field names = new Field("Configuration Trees");
		builder.addField(names);

		for (String name : g.getConfiguration().keySet())
			names.getValue().append(name + '\n');

		builder.buildAndSend();
	}

	public void listNode(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());

		ConfigurationNode node = ConfigurationTree.optAbsoluteNode(g, args.get(0), null);

		if (node == null)
		{
			builder.setDescription("There is no node nor configuration at path '"+ args.get(0) +"'");
			builder.buildAndSend();
			return;
		}

		builder.setDescription("List of '"+ args.get(0) +"' configuration and node children");
		Field name = new Field("Name");
		Field type = new Field("Type");
		Field nature = new Field("Nature");

		for (Entry<String, ConfigurationNode> child : node.getChildren().entrySet())
		{
			name.getValue().append(child.getKey() +'\n');
			if (child.getValue().isConfiguration())
			{
				type.getValue().append(((Configuration<?>) child.getValue()).getType());
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

		ConfigurationNode node = ConfigurationTree.optAbsoluteNode(g, args.get(0), null);

		if (node == null)
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

		ConfigurationNode node = ConfigurationTree.optAbsoluteNode(g, args.get(0), null);

		if (node == null)
		{
			output.getValue().append(CONFIG_CHANGE_FAILED);
			builder.addField("Reason", "There is no such configuration as '"+ args.get(0) +'\'', false);
		}
		else if (node.isConfiguration())
			try
			{
				((Configuration<?>) node).set(args.get(1));
				output.getValue().append("Configuration successfully updated!");
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

	@Override
	public String getSyntaxFile() { return "configuration/config.json"; }
}
