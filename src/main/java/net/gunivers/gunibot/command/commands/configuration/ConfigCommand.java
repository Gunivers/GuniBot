package net.gunivers.gunibot.command.commands.configuration;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Category;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.entity.VoiceChannel;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.parser.Parser;
import net.gunivers.gunibot.datas.Configuration;
import net.gunivers.gunibot.datas.DataCategory;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataMember;
import net.gunivers.gunibot.datas.DataObject;
import net.gunivers.gunibot.datas.DataTextChannel;
import net.gunivers.gunibot.datas.DataVoiceChannel;

public class ConfigCommand extends Command {

	@Override
	public String getSyntaxFile() { return "configuration/config.json"; }

	public void list(MessageCreateEvent event)
	{	
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), "Configuration for server " + g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());
		
		Field names = new Field("Name"); Field types = new Field("Type"); Field targets = new Field("Target");
		for (Configuration<? extends DataObject<?>,?> config : Configuration.all)
		{
			names.getValue().append(config.getName() + '\n');
			types.getValue().append(config.getValueType().getSimpleName() + '\n');
			targets.getValue().append(config.getDataType().getSimpleName().substring(4) + '\n');
		}
		
		builder.addField(names); builder.addField(types); builder.addField(targets);
		builder.buildAndSend();
	}
	
	@SuppressWarnings("unchecked")
	public void get(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), g.getEntity().getName() + "'s Configuration", null);
		builder.setRequestedBy(event.getMember().get());
		
		Configuration.all.stream().filter(c -> c.getName().equalsIgnoreCase(args.get(0))).forEach(config ->
		{
			String desc = "";
			if (config.getDataType() == DataGuild.class) desc = ((Configuration<DataGuild,?>) config).get(g) == null ? "NO_VALUE" : ((Configuration<DataGuild,?>) config).get(g).toString();
			if (config.getDataType() == DataMember.class) desc = ((Configuration<DataMember,?>) config).get(g.getDataMember(event.getMember().get())) == null ? "NO_VALUE" : ((Configuration<DataMember,?>) config).get(g.getDataMember(event.getMember().get())).toString();
			if (config.getDataType() == DataTextChannel.class) desc = ((Configuration<DataTextChannel,?>) config).get(g.getDataTextChannel((TextChannel) event.getMessage().getChannel().block())) == null ? "NO_VALUE" : ((Configuration<DataTextChannel,?>) config).get(g.getDataTextChannel((TextChannel) event.getMessage().getChannel().block())).toString();
			if (config.getDataType() == DataVoiceChannel.class) desc = ((Configuration<DataVoiceChannel,?>) config).get(g.getDataVoiceChannel(event.getMember().get().getVoiceState().block().getChannel().block())) == null ? "NO_VALUE" : ((Configuration<DataVoiceChannel,?>) config).get(g.getDataVoiceChannel(event.getMember().get().getVoiceState().block().getChannel().block())).toString();

			builder.setDescription(config.getName() +":\t"+ desc + (builder.getDescription() == null ? "" : builder.getDescription()));
		});

		if (builder.getDescription() == null)
			builder.setDescription("404: No configuration found for name: "+ args.get(0));
		
		builder.buildAndSend();
	}

	@SuppressWarnings("unchecked")
	public void set(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());

		Member m = args.size() == 2 ? event.getMember().orElse(null)
				: Parser.parseMember(args.get(2), g.getEntity()).blockFirst();
		
		TextChannel tc = args.size() == 2 ? (TextChannel) event.getMessage().getChannel().block()
				: Parser.parseTextChannel(args.get(2), g.getEntity()).blockFirst();
		
		VoiceChannel vc = args.size() == 2 ? event.getMember().get().getVoiceState().blockOptional().isPresent() ?
				event.getMember().get().getVoiceState().block().getChannel().block() : null
				: Parser.parseVoiceChannel(args.get(2), g.getEntity()).blockFirst();
		
		Category ca = args.size() == 2 ? ((TextChannel) event.getMessage().getChannel().block()).getCategory().blockOptional().orElse(null)
				: Parser.parseCategory(args.get(2), g.getEntity()).blockFirst();
		
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), g.getEntity().getName() + "'s Configuration", null);
		builder.setRequestedBy(event.getMember().get());
		
		Configuration.all.stream().filter(c -> c.getName().equalsIgnoreCase(args.get(0))).forEach(c -> {
			try
			{
				builder.setDescription(c.getName());
				
				Field old = new Field("Old value");
				if (c.getDataType() == DataGuild.class)
				{
					old.setValue(String.valueOf(((Configuration<DataGuild, ?>) c).get(g)));
					((Configuration<DataGuild, ?>) c).set(g, args.get(1));
				} else if (c.getDataType() == DataMember.class)
				{
					if (m != null) {
						old.setValue(String.valueOf(((Configuration<DataMember, ?>) c).get(g.getDataMember(m))));
						((Configuration<DataMember, ?>) c).set(g.getDataMember(m), args.get(1));
					} else {
						event.getMessage().getChannel().flatMap(ch -> ch.createMessage("Please be in or provide a valid text channel for this configuration")).subscribe();
						return;
					}
				} else if (c.getDataType() == DataTextChannel.class)
				{
					if (tc != null) {
						old.setValue(String.valueOf(((Configuration<DataTextChannel, ?>) c).get(g.getDataTextChannel(tc))));
						((Configuration<DataTextChannel, ?>) c).set(g.getDataTextChannel(tc), args.get(1));
					} else {
						event.getMessage().getChannel().flatMap(ch -> ch.createMessage("Please be in or provide a valid text channel for this configuration")).subscribe();
						return;
					}
				} else if (c.getDataType() == DataVoiceChannel.class)
				{
					if (vc != null) {
						old.setValue(String.valueOf(((Configuration<DataVoiceChannel, ?>) c).get(g.getDataVoiceChannel(vc))));
						((Configuration<DataVoiceChannel, ?>) c).set(g.getDataVoiceChannel(vc), args.get(1));
					} else {
						event.getMessage().getChannel().flatMap(ch -> ch.createMessage("Please be in or provide a valid voice channel for this configuration")).subscribe();
						return;
					}
				} else if (c.getDataType() == DataCategory.class)
				{
					if (ca != null) {
						old.setValue(String.valueOf(((Configuration<DataCategory, ?>) c).get(g.getDataCategory(ca))));
						((Configuration<DataCategory, ?>) c).set(g.getDataCategory(ca), args.get(1));
					} else {
						event.getMessage().getChannel().flatMap(ch -> ch.createMessage("Please be in or provide a valid category for this configuration")).subscribe();
						return;
					}
				}

				builder.addField(old);
				builder.addField("New Value", args.get(1), true);
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
