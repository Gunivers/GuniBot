package net.gunivers.gunibot.event;

import discord4j.core.event.domain.guild.GuildCreateEvent;
import net.gunivers.gunibot.Main;

public class GuildCreatedListener extends Events<GuildCreateEvent>
{

	protected GuildCreatedListener() { super(GuildCreateEvent.class); }

	@Override protected boolean precondition(GuildCreateEvent event) { return true; }

	@Override
	protected void apply(GuildCreateEvent event)
	{
		if (Main.getDataCenter() == null)
		{
			try { Main.getGuildQueue().put(event); } catch (InterruptedException e)
			{
				System.err.println("Guild queue is full and interrupted ! Skipping guild event !");
			}
		}
		else
			Main.getDataCenter().addGuild(event.getGuild());
	}
}
