package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.TextChannel;

public class DataTextChannel extends DataObject<TextChannel> {

	public DataTextChannel(TextChannel text_channel) {
		super(text_channel);
	}

	public DataTextChannel(TextChannel text_channel, JSONObject json) {
		super(text_channel, json);
	}
}
