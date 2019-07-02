package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.VoiceChannel;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;

class AttachedVoiceChannel extends AttachedDatas<VoiceChannel> {

	AttachedVoiceChannel(VoiceChannel voice_channel, HashMap<String,Object> datas) {
		super(VoiceChannel.class, voice_channel.getId(), datas);
	}

	AttachedVoiceChannel(Snowflake id, HashMap<String,Object> datas) {
		super(VoiceChannel.class, checkId(id), datas);
	}

	private static Snowflake checkId(Snowflake id) {
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getChannelById(id).ofType(VoiceChannel.class)).isPresent()) throw new IllegalArgumentException("This Voice Channel doesn't exist");
		else return id;
	}
}
