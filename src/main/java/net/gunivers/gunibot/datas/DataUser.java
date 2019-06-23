package net.gunivers.gunibot.datas;

import org.json.JSONObject;

import discord4j.core.object.entity.User;

public class DataUser extends DataObject<User>
{

	public DataUser(User user) {
		super(user);
	}


	@Override
	public JSONObject save()
	{
		return super.save();
	}

	@Override
	public void load(JSONObject json)
	{
		super.load(json);
	}
}
