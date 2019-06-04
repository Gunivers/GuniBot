package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.Category;

public class DataCategory extends DataObject<Category> {

	public DataCategory(Category category) {
		super(category);
	}

	public DataCategory(Category category, JSONObject json) {
		super(category, json);
	}

}
