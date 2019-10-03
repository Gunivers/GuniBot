package net.gunivers.gunibot.core.utils;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonObjectV2 extends JSONObject {

	public JsonObjectV2() {}

	public JsonObjectV2(JSONTokener x) throws JSONException {
		super(x);
	}

	public JsonObjectV2(Map<?, ?> m) {
		super(m);
	}

	public JsonObjectV2(Object bean) {
		super(bean);
	}

	public JsonObjectV2(String source) throws JSONException {
		super(source);
	}

	public JsonObjectV2(int initialCapacity) {
		super(initialCapacity);
	}

	public JsonObjectV2(JSONObject jo, String[] names) {
		super(jo, names);
	}

	public JsonObjectV2(Object object, String[] names) {
		super(object, names);
	}

	public JsonObjectV2(String baseName, Locale locale) throws JSONException {
		super(baseName, locale);
	}

	@Override
	public Set<Entry<String, Object>> entrySet() {
		return super.entrySet();
	}

	@Override
	public JsonObjectV2 accumulate(String key, Object value) throws JSONException {
		super.accumulate(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 append(String key, Object value) throws JSONException {
		super.append(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 getJSONObject(String key) throws JSONException {
		return new JsonObjectV2(super.getJSONObject(key));
	}

	@Override
	public JsonObjectV2 increment(String key) throws JSONException {
		super.increment(key);
		return this;
	}

	@Override
	public JsonObjectV2 optJSONObject(String key) {
		JSONObject value = super.optJSONObject(key);
		if(value==null) return null;
		else return new JsonObjectV2(value.toString());
	}

	@Override
	public JsonObjectV2 put(String key, boolean value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 put(String key, Collection<?> value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 put(String key, double value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 put(String key, float value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 put(String key, int value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 put(String key, long value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 put(String key, Map<?, ?> value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 put(String key, Object value) throws JSONException {
		super.put(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 putOnce(String key, Object value) throws JSONException {
		super.putOnce(key, value);
		return this;
	}

	@Override
	public JsonObjectV2 putOpt(String key, Object value) throws JSONException {
		super.putOpt(key, value);
		return this;
	}

}
