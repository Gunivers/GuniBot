package net.gunivers.gunibot.command.commands.developper;

import java.awt.Color;

import discord4j.common.GitProperties;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import net.gunivers.gunibot.core.command.Command;

public class PerfCommand extends Command {

    @Override
    public String getSyntaxFile() {
	return "developper/performance.json";
    }

    public void getPerf(MessageCreateEvent event) {
	Message message = event.getMessage();
	message.getChannel().flatMap(channel -> channel.createEmbed(spec -> {
	    Member author = event.getMember().get();
	    User userBot = event.getClient().getSelf().block();

	    spec.setTitle("Performance report");
	    spec.setAuthor(userBot.getUsername(), null, userBot.getAvatarUrl());
	    spec.setColor(Color.ORANGE);
	    spec.setFooter("Lançé par " + author.getUsername(), author.getAvatarUrl());
	    spec.setTimestamp(message.getTimestamp());

	    double maxMemoryMb = Runtime.getRuntime().maxMemory() / 1024 / 1024;
	    double allocMemoryMb = Runtime.getRuntime().totalMemory() / 1024 / 1024;
	    double freeMemoryMb = Runtime.getRuntime().freeMemory() / 1024 / 1024;
	    double usedMemoryMb = allocMemoryMb - freeMemoryMb;

	    // System.out.println(String.format("Memory total : %s bytes",
	    // Runtime.getRuntime().totalMemory()));
	    // System.out.println(String.format("Memory max : %s bytes",
	    // Runtime.getRuntime().maxMemory()));
	    // System.out.println(String.format("Memory free : %s bytes",
	    // Runtime.getRuntime().freeMemory()));
	    // System.out.println(String.format("Memory total : %s Mb",
	    // Runtime.getRuntime().totalMemory()/1024/1024));
	    // System.out.println(String.format("Memory max : %s Mb",
	    // Runtime.getRuntime().maxMemory()/1024/1024));
	    // System.out.println(String.format("Memory free : %s Mb",
	    // Runtime.getRuntime().freeMemory()/1024/1024));

	    String javaVersion = System.getProperty("java.version", "?");
	    String osType = System.getProperty("os.arch", "?");
	    String apiName = GitProperties.getProperties().getProperty(GitProperties.APPLICATION_NAME, "Discord4j?");
	    String apiVersion = GitProperties.getProperties().getProperty(GitProperties.APPLICATION_VERSION, "3?");

	    spec.addField("OS", osType, true);
	    spec.addField("Java Version", javaVersion, true);
	    spec.addField("Memory used",
		    String.format("%s MB / %s MB / %s MB", usedMemoryMb, allocMemoryMb, maxMemoryMb), true);
	    spec.addField("API Name", apiName, true);
	    spec.addField("API Version", apiVersion, true);
	})).subscribe();
	// System.out.println("PerfCommand#getPerf() executed");
    }

}
