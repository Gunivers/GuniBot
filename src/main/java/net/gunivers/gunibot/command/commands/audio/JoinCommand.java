package net.gunivers.gunibot.command.commands.audio;

import java.util.List;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.VoiceChannel;
import net.gunivers.gunibot.audio.Audio;
import net.gunivers.gunibot.core.command.Command;

public class JoinCommand extends Command {

	@Override
	public String getSyntaxFile() {
		return "audio/join.json";
	}
	
	public void join(MessageCreateEvent e, List<String> args) {
		final Member member = e.getMember().orElse(null);
	    if (member != null) {
	        final VoiceState voiceState = member.getVoiceState().block();
	        if (voiceState != null) {
	            final VoiceChannel channel = voiceState.getChannel().block();
	            if (channel != null) {
	                channel.join(spec -> spec.setProvider(Audio.provider)).block();
	            }
	        }
	    }
	}	
}
