package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.VoiceChannel;

public class DataVoiceChannel extends DataObject<VoiceChannel> {

	public DataVoiceChannel(VoiceChannel voice_channel) {
		super(voice_channel);
	}

	public DataVoiceChannel(VoiceChannel voice_channel, JSONObject json) {
		super(voice_channel, json);
	}

}
