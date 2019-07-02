package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;

class AttachedMember extends DoubleAttachedDatas<Guild, Member> {

	AttachedMember(Member member, HashMap<String,Object> datas) {
		super(Guild.class, member.getGuildId(), Member.class, member.getId(), datas);
	}

	AttachedMember(Snowflake guild_id, Snowflake id, HashMap<String,Object> datas) {
		super(Guild.class, guild_id, Member.class, checkId(guild_id, id), datas);
	}

	private static Snowflake checkId(Snowflake guild_id, Snowflake id) {
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getGuildById(guild_id)).isPresent()) throw new IllegalArgumentException("This Guild doesn't exist");
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getMemberById(guild_id, id)).isPresent()) throw new IllegalArgumentException("This Member doesn't exist");
		else return id;
	}
}
