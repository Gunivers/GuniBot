package net.gunivers.gunibot.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.util.Snowflake;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.datas.DataGuild;

public class FirstConnectionListener extends Events<MemberJoinEvent>
{
	protected FirstConnectionListener() { super(MemberJoinEvent.class); }

	private final ArrayList<Member> history = new ArrayList<>();
	
	@Override protected boolean precondition(MemberJoinEvent event) { return true; }

	@Override
	protected void apply(MemberJoinEvent event)
	{
		DataGuild g = Main.getDataCenter().getDataGuild(event.getGuild().block());
		Member m = event.getMember();
		
		TextChannel tc = g.getWelcomeChannel() == -1L ? null
				: g.getEntity().getChannelById(Snowflake.of(g.getWelcomeChannel())).ofType(TextChannel.class).block();
		
		EmbedBuilder builder = new EmbedBuilder(tc == null ? event.getMember().getPrivateChannel().block() : tc,
				"Welcome to "+ g.getEntity().getName() +'!', null);
		
		builder.setColor(event.getClient().getSelf().block().asMember(event.getGuildId()).block().getColor().block());
		builder.setDescription(g.getWelcomeMessage()
			   .replace("{server}", g.getEntity().getName()).replace("{user}", m.getDisplayName()).replace("{user.mention}", m.getMention()));
		
		builder.buildAndSend();
	}
	
	public List<Member> getHistory() { return Collections.unmodifiableList(history); }
	public void clearHistory() { history.clear(); }
}
