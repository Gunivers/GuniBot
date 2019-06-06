package net.gunivers.gunibot.command.lib.keys;

import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

public class Type implements Key {
	
	private static Type instance;

	private Type() {}
	
	public static Type getInstance() {
		return instance == null ? instance = new Type() : instance;
	}

	@Override
	public String getKey() {
		return "type";
	}

	@Override
	public boolean isMandatory() {
		return true;
	}

	@Override
	public List<KeyEnum> blacklist() {
		return Collections.emptyList();
	}

	@Override
	public void parse(JSONObject obj) {
		
	}

}
