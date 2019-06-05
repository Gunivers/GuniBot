package net.gunivers.gunibot.command.lib.nodes;

import java.util.ArrayList;
import java.util.List;

import discord4j.core.object.entity.Guild;

import net.gunivers.gunibot.command.lib.CommandSyntaxError;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.interfaces.NeedGuild;

public class NodeInfinite extends TypeNode<List<Object>> implements NeedGuild<List<Object>>
{
	private TypeNode<?> type = null;
	
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
	public void parse(String s) throws JsonCommandFormatException
	{
		try
		{
			type = EnumNode.valueOfIgnoreCase(s).createInstance();
			int index = s.indexOf(';'); if (index > -1) type.parse(s.substring(index +1));
		}
		catch (JsonCommandFormatException e) { throw new JsonCommandFormatException("This node cannot be infinite: " + e.getMessage()); }
		catch (Exception e) { throw new JsonCommandFormatException(s + " is not a valid type"); }
	}
}
