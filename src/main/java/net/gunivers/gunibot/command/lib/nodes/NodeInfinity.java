package net.gunivers.gunibot.command.lib.nodes;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Guild;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.interfaces.NeedGuild;

public class NodeInfinity extends TypeNode<List<Object>> implements NeedGuild<List<Object>>
{
	private TypeNode<Object> type = null;
	
	@Override
	public List<Object> getFrom(Guild guild, String list)
	{
		ArrayList<Object> result = new ArrayList<>();
		
		for (String s : list.split(" "))
		{
			if (type instanceof NeedGuild) result.add(((NeedGuild<?>) type).getFrom(guild, s));
			else result.add(type.getFrom(s));
		}
		
		return result;
	}
	
	@Override
	public String getFrom(List<Object> l)
	{
		StringBuilder s = new StringBuilder();
		for (Object o : l) s.append(type.getFrom(o));
		return s.toString();
	}

	@Override
	public CommandSyntaxError matchesNode(Guild guild, String list)
	{
		CommandSyntaxError err = null;
		for (String s : list.split(" "))
		{
			if (type instanceof NeedGuild) err = ((NeedGuild<?>) type).matchesNode(guild, s);
			else err = type.matchesNode(s);
			
			if (err != null) return err;
		}
		
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void parse(String s) throws JsonCommandFormatException
	{
		try
		{
			int index = s.indexOf(';');
			if (index > -1) 
			{
				type = (TypeNode<Object>) EnumNode.valueOfIgnoreCase(s.substring(0, index)).createInstance();
				type.parse(s.substring(index +1));
			} else type = (TypeNode<Object>) EnumNode.valueOfIgnoreCase(s).createInstance();
		}
		catch (JsonCommandFormatException e) { throw e; }
		catch (Exception e) { throw new JsonCommandFormatException(s + " is not a valid type"); }
	}
}
