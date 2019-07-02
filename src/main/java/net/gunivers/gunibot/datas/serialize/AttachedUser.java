package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.User;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;

class AttachedUser extends AttachedDatas<User> {

	AttachedUser(User user, HashMap<String,Object> datas) {
		super(User.class, user.getId(), datas);
	}

	AttachedUser(Snowflake id, HashMap<String,Object> datas) {
		super(User.class, checkId(id), datas);
	}

	private static Snowflake checkId(Snowflake id) {
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getUserById(id)).isPresent()) throw new IllegalArgumentException("This User doesn't exist");
		else return id;
	}
}
