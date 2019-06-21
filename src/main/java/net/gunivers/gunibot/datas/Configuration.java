package net.gunivers.gunibot.datas;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.gunivers.gunibot.core.command.parser.Parser;

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
public class Configuration<D extends DataObject<?>, T>
{
	public static final Set<Configuration<? extends DataObject<?>, ?>> all = new HashSet<>();
	
	public static final Configuration<DataGuild,String> WELCOME_MESSAGE = new Configuration<>("welcome.message", DataGuild::getWelcomeMessage, DataGuild::setWelcomeMessage, (d,s) -> s.trim(), DataGuild.class, String.class);
	public static final Configuration<DataGuild,DataTextChannel> WELCOME_CHANNEL = new Configuration<>("welcome.channel", DataGuild::getWelcomeChannel, DataGuild::setWelcomeChannel, (d,s) -> d.getDataTextChannel(Parser.parseTextChannel(s, d.getEntity()).blockFirst()), DataGuild.class, DataTextChannel.class);

	{
		all.add(this);
	}

	private final String name;
	private final Function<D,T> getter;
	private final BiConsumer<D,T> setter;
	private final BiFunction<D, String, T> parser;
	
	private final Class<D> dclass;
	private final Class<T> vclass;

	/**
	 * Constructs and links a configuration variable to {@link net.gunivers.gunibot.command.commands.configuration.ConfigCommand ConfigCommand}
	 * @param name of this configuration
	 * @param getter the getter method for this variable in the type D.
	 * @param setter the setter method for this variable in the type D.
	 * @param parser a parser that will get a T instance from a String. If data represents a D instance,
	 *        {@code getter.apply(data).equals(parser.apply(data,getter.apply(data)))} should return true.
	 * @param dclass D.class
	 * @param vclass T.class
	 */
	private Configuration(String name, Function<D, T> getter, BiConsumer<D,T> setter, BiFunction<D, String, T> parser, Class<D> dclass, Class<T> vclass)
	{
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.parser = parser;
		
		this.dclass = dclass;
		this.vclass = vclass;
	}

	/**
	 * Call the getter method
	 * @param data the instance which holds the configuration variable
	 * @return getter.apply(data)
	 */
	public T get(D data) { return getter.apply(data); }
	
	/**
	 * Call the parser on the raw input, then call the setter on this variable.
	 * It may cause exceptions if the input is malformated
	 * @param data the instance which holds the configuration variable
	 * @param input the raw String describing the data to set
	 */
	public void set(D data, String input) { setter.accept(data, parser.apply(data, input)); }
	
	public String getName() { return name; }
	public Class<D> getDataType() { return dclass; }
	public Class<T> getValueType() { return vclass; }
}
