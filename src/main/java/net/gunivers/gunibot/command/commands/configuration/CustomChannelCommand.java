package net.gunivers.gunibot.command.commands.configuration;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Category;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.parser.Parser;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.core.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.custom_channel.CustomChannelCreator;
import net.gunivers.gunibot.datas.DataGuild;
import net.gunivers.gunibot.datas.DataTextChannel;

/**
 * 
 * @author A~Z
 * @see CustomChannelCreator
 */
public class CustomChannelCommand extends Command
{
	@Override public String getSyntaxFile() { return "configuration/customchannel.json"; }

	public void list(MessageCreateEvent event, List<String> args)
	{
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel().block(), "Custom Channel's List", null);
		builder.setRequestedBy(event.getMember().get());

		Field names = new Field("Name"); Field owners = new Field("Owner"); Field privacies = new Field("Privacy");
		builder.addField(names); builder.addField(owners); builder.addField(privacies);

		for (DataTextChannel channel : CustomChannelCreator.getByGuild(Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block())))
		{
			names.getValue().append(channel.getEntity().getName() +'\n');
			owners.getValue().append(event.getGuild().block().getMemberById(Snowflake.of(channel.getOwner())).block().getDisplayName() + '\n');
			privacies.getValue().append(channel.isPrivate() +"\n");
		}

		builder.buildAndSend();
	}

	public void add(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block());

		if (!g.isCCEnabled()) {
			event.getMessage().getChannel().flatMap(c -> c.createMessage("Sorry, custom channels are disabled on your server.")).subscribe();
			return;
		}

		if (g.getCCActive() == -1L || g.getCCArchive() == -1L) {
			event.getMessage().getChannel().flatMap(c -> c.createMessage("Your server miss either cchanel.active either cchanel.archive categories configuration")).subscribe();
			return;
		}

		if (CustomChannelCreator.create(event.getMember().get(), Parser.parseCategory(String.valueOf(g.getCCActive()), g.getEntity()).blockFirst(),
				args.get(0), g.getEntity().getChannelById(Snowflake.of(g.getCCArchive())).ofType(Category.class).block(), Boolean.valueOf(args.get(1))))
		{
			DataTextChannel channel = CustomChannelCreator.getByOwner(event.getMember().get());
			event
			.getMessage()
			.getChannel()
			.flatMap(c ->
			c.createMessage(
					channel
					.getEntity()
					.getMention()
					+" successfully created!"))
			.subscribe();
		} else
			event.getMessage().getChannel().flatMap(c -> c.createMessage("Could not create #"+ args.get(0))).subscribe();
	}

	public void del(MessageCreateEvent event)
	{

	}

	public void renew(MessageCreateEvent event)
	{

	}

	public void privacy(MessageCreateEvent event, List<String> args)
	{

	}

	public void invite(MessageCreateEvent event, List<String> args)
	{

	}
}
