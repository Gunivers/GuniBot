package net.gunivers.gunibot.command.commands.fun;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import net.gunivers.gunibot.core.BotUtils;
import net.gunivers.gunibot.core.command.Command;

public class ImitateCommand extends Command {

	public void imitate(MessageCreateEvent e, List<String> args) {
		System.out.println(args.get(0));
		Member member = e.getGuild().block().getMembers()
				.filter(m -> m.getMention().equals(args.get(0)) | m.getDisplayName().equals(args.get(0).substring(0, args.get(0).length() - 5)))
				.blockFirst();
		System.out.println(member);
		if(member == null)
			e.getMessage().getChannel().block().createMessage(args.get(0) + " is not a member of this server!").subscribe();
		else {
			BotUtils.sendMessageWithIdentity(member, e.getMessage().getChannel().block(), args.get(1));
		}
	}
	
	@Override
	public String getSyntaxFile() {
		return "fun/imitate.json";
	}
}
