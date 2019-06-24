package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;
import java.util.HashSet;

import discord4j.core.object.entity.Entity;
import discord4j.core.object.util.Snowflake;

public class Serializer {

	public final String systemId;
	private HashSet<AttachedDatas<Entity>> datas;

	public Serializer(String system_id) {
		systemId = system_id;
		datas = new HashSet<>();
	}

	public void put(Class<Entity> type, Snowflake id, HashMap<String,Object> datas) {
		this.datas.add(new AttachedDatas<>(type, id, datas));
	}

	public HashMap<String,Object> get(Class<Entity> type, Snowflake id) {
		return this.datas.stream().filter(e -> e.type.equals(type) && e.id.equals(id)).findFirst().get().datas;
	}

}
