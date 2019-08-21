package net.gunivers.gunibot.command.commands.developper;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
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
import net.gunivers.gunibot.datas.DataCenter;

public class DataCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "developper/data.json";
	}

	public void displayGuildData(MessageCreateEvent event) {
		Guild guild = event.getGuild().block();

		Message message = event.getMessage();
		message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
			JSONObject json = Main.getBotInstance().getDataCenter().getDataGuild(guild).save();

			String content = String.format("**Data Report** for guild **%s**\n```json\n%s\n```", guild.getName(), json.toString(4));
			if (content.length() > 2000) spec .addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
			else spec.setContent(content);
		})).subscribe();
	}

	public void displayUserData(MessageCreateEvent event, List<String> args) {
		String s_user = args.get(0);
		try {
			User user = Parser.singleEntity(Parser.parseUser(s_user, event.getClient()));

			Message message = event.getMessage();
			message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
				JSONObject json = Main.getBotInstance().getDataCenter().getDataUser(user).save();

				String content = String.format("**Data Report** for user **%s**\n```json\n%s\n```", user.getUsername(), json.toString(4));
				if (content.length() > 2000) spec .addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
				else spec.setContent(content);
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

	public void displaySystemData(MessageCreateEvent event, List<String> args) {
		String system_id = args.get(0);
		Message message = event.getMessage();
		message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
			DataCenter data_center = Main.getBotInstance().getDataCenter();
			if(data_center.isRegisteredSystem(system_id)) {
				JSONObject json = data_center.getSystem(system_id).save();

				String content = String.format("**Data Report** for system **%s**\n```json\n%s\n```", system_id, json.toString(4));
				if (content.length() > 2000) spec .addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
				else spec.setContent(content);
			} else {
				spec.setEmbed(embed_spec -> {
					Member author = event.getMember().get();
					User user_bot = event.getClient().getSelf().block();

					embed_spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					embed_spec.setColor(Color.ORANGE);
					embed_spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					embed_spec.setTimestamp(message.getTimestamp());

					embed_spec.setDescription(String.format("No system '%s' registered!", system_id));
				});
			}

		})).subscribe();
	}

	public void displayOldSerializerData(MessageCreateEvent event, List<String> args) {
		String system_id = args.get(0);
		Message message = event.getMessage();
		message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
			DataCenter data_center = Main.getBotInstance().getDataCenter();
			if(data_center.isRegisteredOldSerializer(system_id)) {
				JSONObject json = data_center.getDataSerializer(system_id);

				String content = String.format("**Data Report** for system **%s**\n```json\n%s\n```", system_id, json.toString(4));
				if (content.length() > 2000) spec .addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
				else spec.setContent(content);
			} else {
				spec.setEmbed(embed_spec -> {
					Member author = event.getMember().get();
					User user_bot = event.getClient().getSelf().block();

					embed_spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
					embed_spec.setColor(Color.ORANGE);
					embed_spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
					embed_spec.setTimestamp(message.getTimestamp());

					embed_spec.setDescription(String.format("No system '%s' registered!", system_id));
				});
			}

		})).subscribe();
	}

}
