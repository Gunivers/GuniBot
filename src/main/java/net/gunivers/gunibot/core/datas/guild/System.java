package net.gunivers.gunibot.core.datas.guild;

import org.json.JSONObject;

import net.gunivers.gunibot.core.datas.config.Configuration;
import net.gunivers.gunibot.core.datas.config.ConfigurationNode;
import net.gunivers.gunibot.core.lib.parsing.commons.BooleanParser;

public class System
{
	protected final ConfigurationNode parent;
	protected final Configuration<Boolean> enabled;

	public System(ConfigurationNode parent)
	{
		this.parent = parent;
		this.enabled = new Configuration<>(parent, "enabled", new BooleanParser(), Configuration.BOOLEAN, true);
	}

	public void load(JSONObject source)
	{
		if (source == null)
			source = new JSONObject();

		this.enabled.setValue(source.optBoolean("enabled", this.enabled.getDefaultValue()));
	}

	public JSONObject save()
	{
		JSONObject obj = new JSONObject();
		obj.put("enabled", this.enabled.getValue());
		return obj;
	}

	public ConfigurationNode parent() { return this.parent; }
	public Configuration<Boolean> enabled() { return this.enabled; }

	public boolean isEnabled() { return this.enabled.getValue(); }
	public void setEnabled(boolean enabled) { this.enabled.setValue(enabled); }

	public String toJSONString() { return this.save().toString(); }
}
