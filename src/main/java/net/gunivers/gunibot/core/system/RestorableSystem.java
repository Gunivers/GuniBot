package net.gunivers.gunibot.core.system;

import org.json.JSONObject;

public interface RestorableSystem extends System {

	public JSONObject save();

	public void load(JSONObject json);

}
