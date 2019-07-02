package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Role;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;

class AttachedRole extends DoubleAttachedDatas<Guild, Role> {

	AttachedRole(Role role, HashMap<String,Object> datas) {
		super(Guild.class, role.getGuildId(), Role.class, role.getId(), datas);
	}

	AttachedRole(Snowflake guild_id, Snowflake id, HashMap<String,Object> datas) {
		super(Guild.class, guild_id, Role.class, checkId(guild_id, id), datas);
	}

	private static Snowflake checkId(Snowflake guild_id, Snowflake id) {
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getGuildById(guild_id)).isPresent()) throw new IllegalArgumentException("This Guild doesn't exist");
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getRoleById(guild_id, id)).isPresent()) throw new IllegalArgumentException("This Role doesn't exist");
		else return id;
	}
}
