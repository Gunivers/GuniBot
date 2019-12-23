package net.gunivers.gunibot.core.datas.config;

import java.io.Serializable;

/**
 * An interface which represents a configurable Object.
 * @author AZ
 * @see Configuration
 * @see ConfigurationNode
 */
@FunctionalInterface
public interface ConfigurationHolder extends Serializable
{
	/**
	 * @return this Object's configuration nicely wrapped because of accessibility issues
	 */
	WrappedConfiguration getConfiguration();
}
