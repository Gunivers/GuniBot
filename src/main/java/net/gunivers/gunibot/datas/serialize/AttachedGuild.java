package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;

class AttachedGuild extends AttachedDatas<Guild> {

	AttachedGuild(Guild guild, HashMap<String,Object> datas) {
		super(Guild.class, guild.getId(), datas);
	}

	AttachedGuild(Snowflake id, HashMap<String,Object> datas) {
		super(Guild.class, checkId(id), datas);
	}

	private static Snowflake checkId(Snowflake id) {
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getGuildById(id)).isPresent()) throw new IllegalArgumentException("This Guild doesn't exist");
		else return id;
	}
}
