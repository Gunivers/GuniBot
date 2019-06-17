package net.gunivers.gunibot.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.az.lib.EmbedBuilder;

public class FirstConnectionListener extends Events<MemberJoinEvent>
{
	protected FirstConnectionListener() { super(MemberJoinEvent.class); }

	private final ArrayList<Member> history = new ArrayList<>();
	
	@Override protected boolean precondition(MemberJoinEvent event) { return true; }

	@Override
	protected void apply(MemberJoinEvent event)
	{
		Guild g = event.getGuild().block();
		Member m = event.getMember();
		
		EmbedBuilder builder = new EmbedBuilder(event.getMember().getPrivateChannel(), "Welcome to "+ g.getName() +'!', null);
		builder.setColor(event.getClient().getSelf().block().asMember(event.getGuildId()).block().getColor().block());
		builder.setDescription(Main.getDataCenter().getDataGuild(g)
				.getWelcome().replace("{server}", g.getName()).replace("{user}", m.getDisplayName()).replace("{user.mention}", m.getMention()));
		
		builder.buildAndSend();
	}
	
	public List<Member> getHistory() { return Collections.unmodifiableList(history); }
	public void clearHistory() { history.clear(); }
}
