package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;

class AttachedTextChannel extends AttachedDatas<TextChannel> {

	AttachedTextChannel(TextChannel text_channel, HashMap<String,Object> datas) {
		super(TextChannel.class, text_channel.getId(), datas);
	}

	AttachedTextChannel(Snowflake id, HashMap<String,Object> datas) {
		super(TextChannel.class, checkId(id), datas);
	}

	private static Snowflake checkId(Snowflake id) {
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getChannelById(id).ofType(TextChannel.class)).isPresent()) throw new IllegalArgumentException("This Text Channel doesn't exist");
		else return id;
	}
}
