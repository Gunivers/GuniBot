package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.Role;

public class DataRole extends DataObject<Role> {

	public DataRole(Role role) {
		super(role);
	}

	public DataRole(Role role, JSONObject json) {
		super(role, json);
	}

}
