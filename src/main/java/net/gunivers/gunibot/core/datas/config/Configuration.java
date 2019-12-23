package net.gunivers.gunibot.core.datas.config;

import java.util.HashSet;
import java.util.Set;

import net.gunivers.gunibot.core.datas.DataObject;
import net.gunivers.gunibot.core.lib.parsing.Parser;
import net.gunivers.gunibot.core.lib.parsing.ParsingException;
import net.gunivers.gunibot.core.utils.TriConsumer;

/**
 * This class intends to manage any configuration. A variable in a {@link DataObject} is deemed configurable as long as it is linked to an
 * instance in this class. Henceforth, any instance of {@link Configuration<D,T>} is visible, gettable, and muttable from the command <b>/config</b>
 * managed by {@link net.gunivers.gunibot.command.commands.configuration.ConfigCommand ConfigCommand}
 *
 * @author AZ
 *
 * @param <D> {@code extends DataObject<?>} the data holder of this configuration variable
 * @param <T> the type of this configuration variable
 */
public class Configuration<T> extends ConfigurationNode
{
	private static final long serialVersionUID = 5367317026511873913L;

	//Displayed types
	public static final String BOOLEAN = "Boolean";
	public static final String BYTE = "Byte";
	public static final String SHORT = "Short Integer";
	public static final String INT = "Integer";
	public static final String LONG = "Long Integer";
	public static final String FLOAT = "Decimal";
	public static final String DOUBLE = "Long Decimal";
	public static final String NUMBER = "Number";
	public static final String CHAR = "Character";
	public static final String STRING = "Characters Chain";
	public static final String LIST = "List";
	public static final String MAP = "Map";
	public static final String SNOWFLAKE = "ID";

	private final Set<TriConsumer<Configuration<T>, T, T>> valueChangedListeners = new HashSet<>();
	private final Parser<T> parser;
	private final String type;

	private T defaultValue;
	private T value;

	/**
	 * Constructs a Configuration
	 * @param parent this node's parent
	 * @param name this node's String identifier
	 * @param parser this configuration's parser: used to parse values from String
	 * @param type this configuration's display type: as it is displayed to the user, it should be as clear as possible
	 * @param value this configuration's default value
	 * @see ConfigurationNode#createConfiguration(String, Parser, String, T)
	 * @see ConfigurationNode#ConfigurationNode(ConfigurationNode, String)
	 */
	Configuration(ConfigurationNode parent, String name, Parser<T> parser, String type, T value)
	{
		super(parent, name);
		this.parser = parser;
		this.type = type;
		this.defaultValue = this.value = value;
	}

	/**
	 * Register a TriConsumer for call upon value change.
	 * @param action a TriConsumer called with: the configuration which value changed [this], the old value, the new value
	 */
	public void onValueChanged(TriConsumer<Configuration<T>, T, T> action) { this.valueChangedListeners.add(action); }

	/**
	 * Notify all registered TriConsumer that this configuration's value changed
	 * @param old the old value
	 * @param val the new value
	 */
	protected void notifyValueChanged(T old, T val) { this.valueChangedListeners.forEach(tc -> tc.accept(this, old, val)); }

	/**
	 * Set this configuration default value. This won't trigger the registered consumers for value modification
	 * @param value the new default value
	 */
	public void setDefaultValue(T value) { this.defaultValue = value; }

	/**
	 * Set this configuration's value to specified. This will trigger the registered consumers for value modification
	 * @param value the new value
	 */
	public void setValue(T value) { this.notifyValueChanged(this.value, value); this.value = value; }

	/**
	 * Try to parse a value from String, then set this configuration's value to the parsed one.
	 * @param input the parsable String
	 * @throws ParsingException if an exception occur while parsing
	 * @see Configuration#setValue(T)
	 * @see Configuration#setOr(String, T)
	 * @see Configuration#setOrDefault(String)
	 */
	public void set(String input) throws ParsingException { this.setValue(this.parser.parse(input)); }

	/**
	 * Try to parse a value from String, then set this configuration's value to the parsed one.
	 * If an error occur, it will set the provided value instead.
	 * @param input the parsable String
	 * @param value the default value
	 * @see Configuration#set(String)
	 * @see Configuration#setOrDefault(String)
	 */
	public void setOr(String input, T value) { try { this.set(input); } catch (ParsingException e) { this.setValue(value); } }

	/**
	 * Try to parse a value from String, then set this configuration's value to the parsed one.
	 * Same as <code><blockquote>configuration.setOr(input, configuration.getDefaultValue())</blockquote></code>
	 * @param input the parsable String
	 * @see Configuration#setOr(String, T)
	 */
	public void setOrDefault(String input) { this.setOr(input, this.getDefaultValue()); }

	/**
	 * Reset this configuration's value to default
	 * Same as <code><blockquote>configuration.setValue(configuration.getDefaultValue())</blockquote></code>
	 */
	public void reset() { this.setValue(this.getDefaultValue()); }

	/** @return this configuration's parser */
	public Parser<T> getParser() { return this.parser; }

	/** @return this configuration's displayed type */
	public String getType() { return this.type; }

	/** @return this configuration's default value */
	public T getDefaultValue() { return this.defaultValue; }

	/** @return this configuration's value */
	public T getValue() { return this.value; }

	/**
	 * @return <code>true</code>
	 * @see ConfigurationNode#isConfiguration()
	 */
	@Override public boolean isConfiguration() { return true; }

	/**
	 * @return <code>this</code>
	 * @see ConfigurationNode#asConfiguration()
	 */
	@Override public Configuration<T> asConfiguration() { return this; }
}
