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
 * @author A~Z
 *
 * @param <D> {@code extends DataObject<?>} the data holder of this configuration variable
 * @param <T> the type of this configuration variable
 */
public class Configuration<T> extends ConfigurationNode
{
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

	public Configuration(ConfigurationNode parent, String name, Parser<T> parser, String type, T value)
	{
		super(parent, name);
		this.parser = parser;
		this.type = type;
		this.defaultValue = this.value = value;
	}

	{
		this.parent.addChild(this);
	}

	public void onValueChanged(TriConsumer<Configuration<T>, T, T> action) { this.valueChangedListeners.add(action); }
	protected void notifyValueChanged(T old, T val) { this.valueChangedListeners.forEach(tc -> tc.accept(this, old, val)); }

	public void setDefaultValue(T value) { this.defaultValue = value; }
	public void setValue(T value) { this.notifyValueChanged(this.value, value); this.value = value; }
	public void set(String input) throws ParsingException { this.setValue(this.parser.parse(input)); }
	public void reset() { this.setValue(this.getDefaultValue()); }

	public Parser<T> getParser() { return this.parser; }
	public String getType() { return this.type; }
	public T getDefaultValue() { return this.defaultValue; }
	public T getValue() { return this.value; }

	@Override public boolean isConfiguration() { return true; }
	@Override public Configuration<T> asConfiguration() { return this; }
}
