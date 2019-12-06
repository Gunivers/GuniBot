package net.gunivers.gunibot.core.datas.guild;

import org.json.JSONObject;

import net.gunivers.gunibot.core.datas.DataGuild;
import net.gunivers.gunibot.core.datas.config.Configuration;
import net.gunivers.gunibot.core.datas.config.ConfigurationNode;
import net.gunivers.gunibot.core.lib.parsing.commons.BooleanParser;

public class System
{
	protected final String name;
	protected final DataGuild guild;
	protected final ConfigurationNode parent;
	protected final Configuration<Boolean> enabled;

	public System(String name, DataGuild guild, ConfigurationNode parent)
	{
		this.name = name;
		this.guild = guild;
		this.parent = parent;
		this.enabled = parent.createConfiguration("enabled", new BooleanParser(), Configuration.BOOLEAN, true);
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

	public String getName() { return this.name; }
	public ConfigurationNode getParent() { return this.parent; }
	public Configuration<Boolean> enabled() { return this.enabled; }

	public boolean isEnabled() { return this.enabled.getValue(); }
	public void setEnabled(boolean enabled) { this.enabled.setValue(enabled); }

	@Override
	public String toString() { return this.save().toString(); }
	public String toJSONString() { return this.save().toString(4); }
}
