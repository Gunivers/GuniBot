package net.gunivers.gunibot.core.datas.config;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

/**
 * A WrappedConfiguration is to be instanciated when a class from the configuration system needs writing access to the configuration map;
 * @author A~Z
 */
public final class WrappedConfiguration implements Serializable
{
	private static final long serialVersionUID = 5143137244967080956L;

	private final Map<String, ConfigurationTree> config;
	private final Map<String, ConfigurationTree> unmodifiable;

	/**
	 * The provided map acts as a pointer, hence should not be immutable
	 * @param configuration a mutable Map
	 */
	public WrappedConfiguration(Map<String, ConfigurationTree> configuration)
	{
		this.config = configuration;
		this.unmodifiable = Collections.unmodifiableMap(this.config);
	}

	/**
	 * @return a mutable {@linkplain Map} containing the configuration
	 */
	Map<String, ConfigurationTree> get() { return this.config; }

	/**
	 * This method return an immutable {@linkplain Map}. As it is backed by the provided Map, changes into the latter are reflected into the
	 * immutable one.
	 * @return an immutable Map
	 */
	public Map<String, ConfigurationTree> asMap() { return this.unmodifiable; }
}
