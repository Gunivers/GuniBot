package net.gunivers.gunibot.core.datas.config;

import java.util.Collections;
import java.util.Map;

public final class WrappedConfiguration
{
	private Map<String, ConfigurationTree> config;

	/**
	 * The provided configuration map acts as a pointer.
	 * @param configuration the real configuration map, not a duplicate to avoid modifications
	 */
	public WrappedConfiguration(Map<String, ConfigurationTree> configuration)
	{
		this.config = configuration;
	}

	Map<String, ConfigurationTree> get() { return this.config; }

	/**
	 * @return an unmodifiable Configuration map
	 */
	public Map<String, ConfigurationTree> asMap() { return Collections.unmodifiableMap(this.config); }
}
