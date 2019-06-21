package net.gunivers.gunibot.command.commands.administrator;

import java.awt.Color;
import java.util.List;

import org.json.JSONObject;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.parser.ObjectParsingException;
import net.gunivers.gunibot.core.command.parser.Parser;

public class DataCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "administrator/data.json";
	}

	public void displayGuildData(MessageCreateEvent event) {
		Guild guild = event.getGuild().block();

		Message message = event.getMessage();
		message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
			JSONObject json = Main.getDataCenter().getDataGuild(guild).save();
			spec.setContent(String.format("**Data Report** for guild **%s**\n```json\n%s\n```", guild.getName(), json.toString(4)));
		})).subscribe();
	}

	public void displayUserData(MessageCreateEvent event, List<String> args) {
		String s_user = args.get(0);
		try {
			User user = Parser.singleEntity(Parser.parseUser(s_user, event.getClient()));

			Message message = event.getMessage();
			message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
				JSONObject json = Main.getDataCenter().getDataUser(user).save();
				spec.setContent(String.format("**Data Report** for user **%s**\n```json\n%s\n```", user.getUsername(), json.toString(4)));
			})).subscribe();
		} catch (ObjectParsingException e) {
			Message message = event.getMessage();
			message.getChannel().flatMap(channel -> channel.createEmbed(embed_spec -> {
				Member author = event.getMember().get();
				User user_bot = event.getClient().getSelf().block();

				embed_spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
				embed_spec.setColor(Color.ORANGE);
				embed_spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
				embed_spec.setTimestamp(message.getTimestamp());

				embed_spec.setDescription(e.getMessage());
			})).subscribe();
		}


	}

}
