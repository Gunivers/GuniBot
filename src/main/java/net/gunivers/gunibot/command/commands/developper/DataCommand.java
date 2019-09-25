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

	    String content = String.format("**Data Report** for guild **%s**\n```json\n%s\n```", guild.getName(),
		    json.toString(4));
	    if (content.length() > 2000) {
		spec.addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
	    } else {
		spec.setContent(content);
	    }
	})).subscribe();
    }

    public void displayUserData(MessageCreateEvent event, List<String> args) {
	String strUser = args.get(0);
	try {
	    User user = Parser.singleEntity(Parser.parseUser(strUser, event.getClient()));

	    Message message = event.getMessage();
	    message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
		JSONObject json = Main.getBotInstance().getDataCenter().getDataUser(user).save();

		String content = String.format("**Data Report** for user **%s**\n```json\n%s\n```", user.getUsername(),
			json.toString(4));
		if (content.length() > 2000) {
		    spec.addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		} else {
		    spec.setContent(content);
		}
	    })).subscribe();

	} catch (ObjectParsingException e) {
	    Message message = event.getMessage();
	    message.getChannel().flatMap(channel -> channel.createEmbed(embedSpec -> {
		Member author = event.getMember().get();
		User userBot = event.getClient().getSelf().block();

		embedSpec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		embedSpec.setColor(Color.ORANGE);
		embedSpec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		embedSpec.setTimestamp(message.getTimestamp());

		embedSpec.setDescription(e.getMessage());
	    })).subscribe();
	}
    }

    public void displaySystemData(MessageCreateEvent event, List<String> args) {
	String systemId = args.get(0);
	Message message = event.getMessage();
	message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
	    DataCenter dataCenter = Main.getBotInstance().getDataCenter();
	    if (dataCenter.isRegisteredSystem(systemId)) {
		JSONObject json = dataCenter.getSystem(systemId).save();

		String content = String.format("**Data Report** for system **%s**\n```json\n%s\n```", systemId,
			json.toString(4));
		if (content.length() > 2000) {
		    spec.addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		} else {
		    spec.setContent(content);
		}
	    } else {
		spec.setEmbed(embedSpec -> {
		    Member author = event.getMember().get();
		    User userBot = event.getClient().getSelf().block();

		    embedSpec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    embedSpec.setColor(Color.ORANGE);
		    embedSpec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    embedSpec.setTimestamp(message.getTimestamp());

		    embedSpec.setDescription(String.format("No system '%s' registered!", systemId));
		});
	    }

	})).subscribe();
    }

    public void displayOldSerializerData(MessageCreateEvent event, List<String> args) {
	String systemId = args.get(0);
	Message message = event.getMessage();
	message.getChannel().flatMap(channel -> channel.createMessage(spec -> {
	    DataCenter dataCenter = Main.getBotInstance().getDataCenter();
	    if (dataCenter.isRegisteredOldSerializer(systemId)) {
		JSONObject json = dataCenter.getDataSerializer(systemId);

		String content = String.format("**Data Report** for system **%s**\n```json\n%s\n```", systemId,
			json.toString(4));
		if (content.length() > 2000) {
		    spec.addFile("output.txt", new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
		} else {
		    spec.setContent(content);
		}
	    } else {
		spec.setEmbed(embedSpec -> {
		    Member author = event.getMember().get();
		    User userBot = event.getClient().getSelf().block();

		    embedSpec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
		    embedSpec.setColor(Color.ORANGE);
		    embedSpec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
		    embedSpec.setTimestamp(message.getTimestamp());

		    embedSpec.setDescription(String.format("No system '%s' registered!", systemId));
		});
	    }

	})).subscribe();
    }

}
