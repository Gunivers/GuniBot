package net.gunivers.gunibot.command.lib.nodes.interfaces;

import discord4j.core.object.entity.Guild;
import net.gunivers.gunibot.command.lib.CommandSyntaxError;

public interface NeedGuild<T> extends Gettable<T>
{
	public default T getFrom(String s) { try { return getFrom(null, s); } catch (Throwable t) { return null; }}
	public default CommandSyntaxError matchesNode(String s) { try { return matchesNode(null, s); } catch (Throwable t)
	{
		return new CommandSyntaxError(
				"```❌  An error occured while running the command.```"
				+ " If this persists, please contact this bot developpers on Gunivers"
				+ "\n||https://discord.gg/EncRXj2||");
	}}
	
	public abstract T getFrom(Guild guild, String s);
	public CommandSyntaxError matchesNode(Guild guild, String s);
}
