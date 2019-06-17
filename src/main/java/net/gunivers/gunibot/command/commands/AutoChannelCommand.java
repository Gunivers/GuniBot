package net.gunivers.gunibot.command.commands;

import java.util.List;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.auto_vocal_channel.VoiceChannelCreator;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder.Field;
import net.gunivers.gunibot.core.command.Command;

public class AutoChannelCommand extends Command {
	
	public void registerChannel(MessageCreateEvent e, List<String> args) {
		try {
			GuildChannel sf = e.getGuild().block().getChannelById(Snowflake.of(args.get(0))).block();
			if(VoiceChannelCreator.isVoiceChannelCreator((VoiceChannel)sf)) {
				VoiceChannelCreator.removeVoiceChannelCreator((VoiceChannel)sf);
				e.getMessage().getChannel().block().createMessage(((VoiceChannel)sf).getName() + " is no longer an Auto Channel!").subscribe();
			} else {
				VoiceChannelCreator.addVoiceChannelCreator((VoiceChannel)sf);
				e.getMessage().getChannel().block().createMessage(((VoiceChannel)sf).getName() + " is now an Auto Channel!").subscribe();
			}
		} catch(Exception exc) {
			e.getMessage().getChannel().block().createMessage("Snowflake not valid!").subscribe();
		}
	}
	
	public void list(MessageCreateEvent e) {
		final EmbedBuilder builder = new EmbedBuilder(e.getMessage().getChannel(), "Autochannel List", null);
		builder.setAuthor(e.getMember().get());
		final Field list = new Field("");
		list.getValue().append(
				VoiceChannelCreator.getVoiceChannelCreator()
				.stream().map(s -> e.getGuild().block().getChannelById(s).doOnError(null).block())
				.filter(c -> c != null && c instanceof VoiceChannel)
				.map(c -> c.getName())
				.collect(Collectors.joining("\n"))
		);
		builder.addField(list);
		builder.buildAndSend();
	}

	@Override
	public String getSyntaxFile() {
		return "autochannel.json";
	}

}
