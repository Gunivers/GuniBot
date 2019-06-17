package net.gunivers.gunibot.datas;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Configuration<T>
{
	public static final Set<Configuration<?>> all = new HashSet<>();
	
	public static final Configuration<String> WELCOME = new Configuration<>("welcome", DataGuild::getWelcome, DataGuild::setWelcome, String.class, String::trim);
	
	{
		all.add(this);
	}
	
	private final String name;
	private final Function<DataGuild, T> getter;
	private final BiConsumer<DataGuild, T> setter;
	
	private final Class<T> clazz;
	private final Function<String, T> parser;
	
	private Configuration(String name, Function<DataGuild, T> getter, BiConsumer<DataGuild, T> setter, Class<T> clazz, Function<String, T> parser)
	{
		this.name = name;
		this.getter = getter;
		this.setter = setter;
		this.clazz = clazz;
		
		this.parser = parser;
	}

	public Object get(DataGuild g) { return getter.apply(g); }
	public void set(DataGuild g, String input) { setter.accept(g, parser.apply(input)); }
	
	public String getName() { return name; }
	public Class<T> getType() { return clazz; }
}
