package net.gunivers.gunibot.datas.serialize;

import java.util.HashMap;

import discord4j.core.object.entity.Entity;
import discord4j.core.object.util.Snowflake;

abstract class DoubleAttachedDatas<E extends Entity, S extends Entity> extends AttachedDatas<E>{

	public final Class<S> subType;
	public final Snowflake subId;

	DoubleAttachedDatas(Class<E> type, Snowflake id, Class<S> sub_type, Snowflake sub_id, HashMap<String,Object> datas) {
		super(type, id, datas);
		subType = sub_type;
		subId = sub_id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((subId == null) ? 0 : subId.hashCode());
		result = prime * result + ((subType == null) ? 0 : subType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("unchecked")
		DoubleAttachedDatas<E,S> other = (DoubleAttachedDatas<E,S>) obj;
		if (subId == null) {
			if (other.subId != null)
				return false;
		} else if (!subId.equals(other.subId))
			return false;
		if (subType == null) {
			if (other.subType != null)
				return false;
		} else if (!subType.equals(other.subType))
			return false;
		return true;
	}
}
