package net.gunivers.gunibot.command.commands;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import discord4j.core.event.domain.message.MessageCreateEvent;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.datas.DataGuild;

public class ConfigCommand extends Command {

	@Override
	public String getSyntaxFile() { return "config.json"; }

	public void list(MessageCreateEvent event)
	{	
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		Set<Field> s = Arrays.asList(g.getClass().getDeclaredFields()).stream().filter(f -> Modifier.isPublic(f.getModifiers())).collect(Collectors.toSet());
		
		EmbedBuilder builder = new EmbedBuilder(event.getMessage().getChannel(), "Server Configuration for "+ g.getEntity().getName(), null);
		builder.setRequestedBy(event.getMember().get());
		
		EmbedBuilder.Field names = new EmbedBuilder.Field("Name");
		names.setValue(s.stream().map(Field::getName).reduce("", (a,b) -> a +" - "+ b +":\n"));
		builder.addField(names);
		
		EmbedBuilder.Field types = new EmbedBuilder.Field("Type");
		types.setValue(s.stream().map(Field::getType).map(Class::getSimpleName).reduce("", (a,b) -> a + b +'\n'));
		builder.addField(types);

		builder.buildAndSend();
	}
	
	public void get(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		
		try
		{
			String value = g.getClass().getDeclaredField(args.get(0)).get(g).toString();
			event.getMessage().getChannel().flatMap(c -> c.createMessage(args.get(0) +": "+ value)).subscribe();
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
		{
			event.getMessage().getChannel().flatMap(c -> c.createMessage("There is no accessible field such as "+ args.get(0))).subscribe();
		}
	}
	
	public void set(MessageCreateEvent event, List<String> args)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		
		try
		{
			Field f = g.getClass().getField(args.get(0)); f.setAccessible(true);
			Class<?> type = f.getType();
			
			try
			{
					 if (type == boolean.class || type == Boolean.class) f.setBoolean(g, Boolean.parseBoolean(args.get(1)));
				else if (type == byte.class || type == Byte.class) f.setByte(g, Byte.parseByte(args.get(1)));
				else if (type == double.class || type == Double.class) f.setDouble(g, Double.parseDouble(args.get(1)));
				else if (type == float.class || type == Float.class) f.setFloat(g, Float.parseFloat(args.get(1)));
				else if (type == int.class || type == Integer.class) f.setInt(g, Integer.parseInt(args.get(1)));
				else if (type == long.class || type == Long.class) f.setLong(g, Long.parseLong(args.get(1)));
				else if (type == short.class || type == Short.class) f.setShort(g, Short.parseShort(args.get(1)));
				else if (type == char.class || type == Character.class) f.setChar(g, args.get(1).charAt(0));
				else if (type == String.class) f.set(g, args.get(1));
				else {
					event.getMessage().getChannel().flatMap(c -> c.createMessage("You may not assign a field of class "+ type.getSimpleName())).subscribe();
					return;
				}
				
				event.getMessage().getChannel().flatMap(c -> c.createMessage("Successfully set '"+ args.get(1) +"' for '"+ f.getName() +'\'')).subscribe();
			} catch (IllegalArgumentException e)
			{
				event.getMessage().getChannel().flatMap(c -> c.createMessage("The value '"+ args.get(1) +"' should be "+ type.getSimpleName())).subscribe();
			}
			
		} catch (NoSuchFieldException | SecurityException | IllegalAccessException e)
		{
			event.getMessage().getChannel().flatMap(c -> c.createMessage("There is no accessible field of name '"+ args.get(0) +'\'')).subscribe();
		}
	}
}
