package net.gunivers.gunibot.datas;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import net.gunivers.gunibot.core.command.parser.Parser;

public class Configuration<D extends DataObject<?>, T>
{
	public static final Set<Configuration<? extends DataObject<?>, ?>> all = new HashSet<>();
	
	public static final Configuration<DataGuild,String> WELCOME_MESSAGE = new Configuration<>("welcome.message", DataGuild::getWelcomeMessage, DataGuild::setWelcomeMessage, (d,s) -> s.trim(), DataGuild.class, String.class);
	public static final Configuration<DataGuild,DataTextChannel> WELCOME_CHANNEL = new Configuration<>("welcome.channel", DataGuild::getWelcomeChannel, DataGuild::setWelcomeChannel, (d,s) -> d.getDataTextChannel(Parser.parseTextChannel(s,d.getEntity()).blockFirst()), DataGuild.class, DataTextChannel.class);
	
	{
		all.add(this);
	}

	private final String name;
	private final Function<D,T> getter;
	private final BiConsumer<D,T> setter;
	private final BiFunction<D, String, T> parser;
	
	private final Class<D> dclass;
	private final Class<T> vclass;
	
	private Configuration(String name, Function<D, T> getter, BiConsumer<D,T> setter, BiFunction<D, String, T> parser, Class<D> dclass, Class<T> vclass)
	{
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.parser = parser;
		
		this.dclass = dclass;
		this.vclass = vclass;
	}

	public Object get(D data) { return getter.apply(data); }
	public void set(D data, String input) { setter.accept(data, parser.apply(data, input)); }
	
	public String getName() { return name; }
	public Class<D> getDataType() { return dclass; }
	public Class<T> getValueType() { return vclass; }
}
