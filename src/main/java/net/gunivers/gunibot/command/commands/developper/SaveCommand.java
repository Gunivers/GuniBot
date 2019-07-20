package net.gunivers.gunibot.command.commands.developper;

import java.awt.Color;
import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.parser.ObjectParsingException;
import net.gunivers.gunibot.core.command.parser.Parser;

public class SaveCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "developper/save.json";
	}

	public void saveGuild(MessageCreateEvent event) {
		Guild guild = event.getGuild().block();
		Main.getBotInstance().getDataCenter().saveGuild(guild);

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

	public void saveUser(MessageCreateEvent event, List<String> args) {
		Message message = event.getMessage();
		User user;

		try {
			user = Parser.singleEntity(Parser.parseUser(args.get(0), event.getClient()));
		} catch (ObjectParsingException e) {
			message.getChannel().flatMap(channel -> channel.createEmbed(spec -> {
				Member author = event.getMember().get();
				User user_bot = event.getClient().getSelf().block();

				spec.setTitle("Error in data saving !");
				spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
				spec.setColor(Color.ORANGE);
				spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
				spec.setTimestamp(message.getTimestamp());

				spec.setDescription(e.getMessage());
			})).subscribe();
			return;
		}
		Main.getBotInstance().getDataCenter().saveUser(user);

		message.getChannel().flatMap(channel -> channel.createEmbed(spec -> {
			Member author = event.getMember().get();
			User user_bot = event.getClient().getSelf().block();

			spec.setTitle("Data saving");
			spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
			spec.setColor(Color.ORANGE);
			spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
			spec.setTimestamp(message.getTimestamp());

			spec.setDescription("All datas of the user "+user.getUsername()+" has been saved!");
		})).subscribe();
	}

	public void saveSystem(MessageCreateEvent event, List<String> args) {
		Message message = event.getMessage();
		String system_id = args.get(0);
		Main.getBotInstance().getDataCenter().saveSystem(system_id);

		message.getChannel().flatMap(channel -> channel.createEmbed(spec -> {
			Member author = event.getMember().get();
			User user_bot = event.getClient().getSelf().block();

			spec.setTitle("Data saving");
			spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
			spec.setColor(Color.ORANGE);
			spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
			spec.setTimestamp(message.getTimestamp());

			spec.setDescription("All datas of the system "+system_id+" has been saved!");
		})).subscribe();
	}

}
