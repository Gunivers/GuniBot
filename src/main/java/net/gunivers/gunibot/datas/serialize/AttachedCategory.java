package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.Category;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.BotUtils;

class AttachedCategory extends AttachedDatas<Category> {

	AttachedCategory(Category category, HashMap<String,Object> datas) {
		super(Category.class, category.getId(), datas);
	}

	AttachedCategory(Snowflake id, HashMap<String,Object> datas) {
		super(Category.class, checkId(id), datas);
	}

	private static Snowflake checkId(Snowflake id) {
		if(!BotUtils.returnOptional(Main.getBotInstance().getBotClient().getChannelById(id).ofType(Category.class)).isPresent()) throw new IllegalArgumentException("This Category doesn't exist");
		else return id;
	}
}
