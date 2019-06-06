package net.gunivers.gunibot.command.lib.keys;

import java.util.List;

import org.json.JSONObject;

public interface Key {

	public String getKey();
	public boolean isMandatory();
	public List<KeyEnum> blacklist();
	public void parse(JSONObject obj);

}
