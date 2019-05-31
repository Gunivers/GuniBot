package net.gunivers.gunibot.command.commands;

import java.awt.Color;

import discord4j.common.GitProperties;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.gunivers.gunibot.command.lib.Command;

public class PerfCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "performance.json";
	}

	public void getPerf(MessageCreateEvent event) {
		Message message = event.getMessage();
		message.getChannel().flatMap(channel -> channel.createEmbed(spec -> {
			Member author = event.getMember().get();
			User user_bot = event.getClient().getSelf().block();

			spec.setTitle("Performance report");
			spec.setAuthor(user_bot.getUsername(), null, user_bot.getAvatarUrl());
			spec.setColor(Color.ORANGE);
			spec.setFooter("Lançé par "+author.getUsername(), author.getAvatarUrl());
			spec.setTimestamp(message.getTimestamp());



			double max_mem_mb = Runtime.getRuntime().maxMemory() / 1024/1024;
			double alloc_mem_mb = Runtime.getRuntime().totalMemory() / 1024/1024;
			double free_mem_mb = Runtime.getRuntime().freeMemory() / 1024/1024;
			double used_mem_mb = alloc_mem_mb - free_mem_mb;

			//			System.out.println(String.format("Memory total : %s bytes", Runtime.getRuntime().totalMemory()));
			//			System.out.println(String.format("Memory max : %s bytes", Runtime.getRuntime().maxMemory()));
			//			System.out.println(String.format("Memory free : %s bytes", Runtime.getRuntime().freeMemory()));
			//			System.out.println(String.format("Memory total : %s Mb", Runtime.getRuntime().totalMemory()/1024/1024));
			//			System.out.println(String.format("Memory max : %s Mb", Runtime.getRuntime().maxMemory()/1024/1024));
			//			System.out.println(String.format("Memory free : %s Mb", Runtime.getRuntime().freeMemory()/1024/1024));

			String java_version = System.getProperty("java.version","?");
			String os_type = System.getProperty("os.arch","?");
			String api_name = GitProperties.getProperties().getProperty(GitProperties.APPLICATION_NAME, "Discord4j?");
			String api_version = GitProperties.getProperties().getProperty(GitProperties.APPLICATION_VERSION, "3?");

			spec.addField("OS", os_type, true);
			spec.addField("Java Version", java_version, true);
			spec.addField("Memory used", String.format("%s MB / %s MB / %s MB", used_mem_mb, alloc_mem_mb, max_mem_mb), true);
			spec.addField("API Name", api_name, true);
			spec.addField("API Version", api_version, true);
		})).subscribe();
		//System.out.println("PerfCommand#getPerf() executed");
	}

}
