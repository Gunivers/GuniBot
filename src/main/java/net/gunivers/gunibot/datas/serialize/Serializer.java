package net.gunivers.gunibot.datas.serialize;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class Serializer extends HashMap<String, Serializable> {

	private static final long serialVersionUID = 1808466229478997381L;

	public Serializer() {
		super();
	}

	public JSONObject toJson() {
		return new JSONObject(this);
	}

	@SuppressWarnings("unchecked")
	public static Serializer from(JSONObject json) {
		Serializer output = new Serializer();
		Map<String, Object> data_map = json.toMap();
		output.putAll((Map<? extends String, ? extends Serializable>) data_map);
		return output;
	}
}
