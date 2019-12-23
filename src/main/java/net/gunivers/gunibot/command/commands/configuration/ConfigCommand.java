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
	/** Message used when this failed to change a configuration's value */
	private static final String CONFIG_CHANGE_FAILED = "FAILURE";

	/**
	 * Displays a list of all visible configuration trees within a {@linkplain DataGuild}
	 * @param event a {@linkplain MessageCreateEvent}
	 */
	public void list(MessageCreateEvent event)
	{
		//Get DataGuild from Guild
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get()); //Set the color of the embed into the member's, and add a footer "Requested by: ..."

		//Add a field displayed as "Configuration Trees" in the embed
		Field names = new Field("Configuration Trees");
		builder.addField(names);

		//Fill the field with the configuration trees names
		for (Entry<String, ConfigurationTree> config : g.getConfiguration().asMap().entrySet())
			if (config.getValue().isVisible())
				names.getValue().append(config.getKey() + '\n');

		//Send the embed
		builder.buildAndSend();
	}

	/**
	 * Display a list of all visibles children of a configuration node
	 * @param event a {@linkplain MessageCreateEvent}
	 * @param args solely the absolute path of the node
	 */
	public void listNode(MessageCreateEvent event, List<String> args)
	{
		//Get DataGuild from Guild
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get()); //Set the color of the embed into the member's, and add a footer "Requested by: ..."

		//Try to get a node within a guild's configuration tree with the provided absolute path
		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		//If the node is invisible, it is treated as non-existent
		if (node == null || !node.isVisible())
		{
			builder.setDescription("404: Node '"+ args.get(0) +"' not found!");
			builder.buildAndSend();
			return;
		}

		builder.setDescription('`'+ args.get(0) +'`'); //Display the targeted node in the embed's description
		Field name = new Field("Name");		//Add field for the node name
		Field type = new Field("Type");		//Add field for the node type [String, boolean, ...]
		Field nature = new Field("Nature"); //Add field for the node nature [Configuration ?]

		//Display all visibles children of the targeted node
		for (Entry<String, ConfigurationNode> child : node.getChildren().entrySet()) if (child.getValue().isVisible())
		{
			//Display name
			name.getValue().append(child.getKey() +'\n');
			if (child.getValue().isConfiguration())
			{
				type.getValue().append(child.getValue().asConfiguration().getType() +'\n'); //Display the configuration's type
				nature.getValue().append("CONFIGURATION\n"); //Display the configuration's nature
			} else
			{
				type.getValue().append("─\n"); //Nodes don't have value types
				nature.getValue().append("NODE\n"); //Display the node's nature
			}
		}

		//Register fields
		builder.addField(name);
		builder.addField(type);
		builder.addField(nature);
		builder.buildAndSend(); //Send the embed
	}

	/**
	 * Get the full description of a node
	 * @param event a {@linkplain MessageCreateEvent}
	 * @param args the node's absolute path
	 */
	public void get(MessageCreateEvent event, List<String> args)
	{
		//Get DataGuild from Guild
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get()); //Set the color of the embed into the member's, and add a footer "Requested by: ..."

		//Try to get a node within a guild's configuration tree with the provided absolute path
		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		//If the node is invisible, it is treated as non-existent
		if (node == null || !node.isVisible())
		{
			builder.setDescription("There is no node nor configuration at path '"+ args.get(0) +"'");
			builder.buildAndSend();
			return;
		}

		//Display the node's name
		builder.setDescription('`'+ node.getName() +'`');

		if (node.isConfiguration())
		{
			Configuration<?> config = node.asConfiguration();
			builder.addField("Nature", "CONFIGURATION", true);				//Display the configuration's nature
			builder.addField("Value", config.getValue().toString(), true);	//Display the configuration's value
			builder.addField("Type", config.getType(), true);				//Display the configuration's type
		} else
			builder.addField("Nature", "NODE", true); //Display the node's nature

		builder.addField("Children", String.valueOf(node.getChildren().size()), true);	//Display the amount of children for the node
		builder.addField("Path", node.getPath(), true);	//Display the node's path
		builder.buildAndSend();
	}

	/**
	 * Modify the value of the node targeted with the specified absolute path
	 * @param event a {@linkplain MessageCreateEvent}
	 * @param args the node's absolute path ; the String representation of a value
	 */
	public void set(MessageCreateEvent event, List<String> args)
	{
		//Get DataGuild from Guild
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get()); //Set the color of the embed into the member's, and add a footer "Requested by: ..."

		//Add a field containing the result of this operation
		Field output = new Field("Result", false);
		builder.addField(output);

		//Try to get a node within a guild's configuration tree with the provided absolute path
		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		//If the node is invisible, it is treated as non-existent
		if (node == null || !node.isVisible())
		{
			output.getValue().append(CONFIG_CHANGE_FAILED);
			builder.addField("Reason", "404: Configuration '"+ args.get(0) +"' not found!", false); //Display the reason o the failure
		}
		else if (node.isConfiguration())
			try
			{
				Configuration<?> config = node.asConfiguration();
				Field old = new Field("Old Value", String.valueOf(config.getValue())); //Memorize the old configuration's value

				//Set the configuration's value, throw a ParsingException if parsing failed
				config.set(args.get(1));
				output.getValue().append("SUCCESS"); //Tell the user the operation succeeded

				//Display the old and new values of the configuration
				builder.addField(old);
				builder.addField("New Value", String.valueOf(config.getValue()), true);
			} catch (ParsingException e) //If the parsing failed
			{
				output.getValue().append(CONFIG_CHANGE_FAILED);
				builder.addField("Reason", e.getMessage(), false); //Display the reason of the failure
			}
		else //If the node is not a configuration
		{
			output.getValue().append(CONFIG_CHANGE_FAILED);
			builder.addField("Reason", "Node '"+ args.get(0) +"' holds no configuration!", false); //Display the reason of the failure
		}

		//Send the embed
		builder.buildAndSend();
	}

	/**
	 * Reset a configuration to its default value
	 * @param event a {@linkplain MessageCreateEvent}
	 * @param args the configuration's absolute path
	 */
	public void reset(MessageCreateEvent event, List<String> args)
	{
		//Get DataGuild from Guild
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get()); //Set the color of the embed into the member's, and add a footer "Requested by: ..."

		//Try to get a node within a guild's configuration tree with the provided absolute path
		ConfigurationNode node = ConfigurationTree.getAbsoluteNode(g, args.get(0));

		//If the node is invisible, it is treated as non-existent
		if (node == null || !node.isVisible())
			builder.setDescription("404: Configuration '"+ args.get(0) +"' not found!");
		else if (node.isConfiguration())
		{
			Configuration<?> config = node.asConfiguration();
			builder.setDescription('\''+ args.get(0) +"' successfully reseted!"); //Tell the user the operation succeeded

			builder.addField("Old Value", String.valueOf(config.getValue()), true); //Display the old value
			config.reset(); //Reset the configuration
			builder.addField("New Value", String.valueOf(config.getValue()), true); //Display the new value
		}
		else
			builder.setDescription("Node '"+ args.get(0) + "' holds no configuration");

		//Send the embed
		builder.buildAndSend();
	}

	@Override
	public String getSyntaxFile() { return "configuration/config.json"; }
}
