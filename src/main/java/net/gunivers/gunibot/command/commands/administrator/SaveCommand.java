package net.gunivers.gunibot.command.commands.administrator;

import java.awt.Color;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;

public class SaveCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "administrator/save.json";
	}

	public void save(MessageCreateEvent event) {
		Guild guild = event.getGuild().block();
		Main.getDataCenter().saveGuild(guild);

		Message message = event.getMessage();
		message.getChannel().flatMap(channel -> channel.createEmbed(spec -> {
			Member author = event.getMember().get();
			User user_bot = event.getClient().getSelf().block();

			spec.setTitle("Data saving");
			spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
			spec.setColor(Color.ORANGE);
			spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
			spec.setTimestamp(message.getTimestamp());

			spec.setDescription("All datas of this server has been saved!");
		})).subscribe();
	}

}
