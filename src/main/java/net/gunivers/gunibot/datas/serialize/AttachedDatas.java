package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.Entity;
import discord4j.core.object.util.Snowflake;

class AttachedDatas<E extends Entity> {

	public final Class<E> type;
	public final Snowflake id;
	public final HashMap<String,Object> datas;

	AttachedDatas(Class<E> type, Snowflake id, HashMap<String,Object> datas) {
		this.type = type;
		this.id = id;
		this.datas = datas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		AttachedDatas<E> other = (AttachedDatas<E>) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
