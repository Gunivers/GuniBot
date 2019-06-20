package net.gunivers.gunibot.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;

import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.lib.EmbedBuilder;
import net.gunivers.gunibot.datas.Configuration;
import net.gunivers.gunibot.datas.DataTextChannel;

import reactor.core.publisher.Mono;

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
		DataTextChannel tc = Configuration.WELCOME_CHANNEL.get(Main.getDataCenter().getDataGuild(g));
		
		EmbedBuilder builder = new EmbedBuilder(tc == null ? event.getMember().getPrivateChannel() : Mono.just(tc.getEntity()),
				"Welcome to "+ g.getName() +'!', null);
		
		builder.setColor(event.getClient().getSelf().block().asMember(event.getGuildId()).block().getColor().block());
		builder.setDescription(String.valueOf(Configuration.WELCOME_MESSAGE.get(Main.getDataCenter().getDataGuild(g)))
				.replace("{server}", g.getName()).replace("{user}", m.getDisplayName()).replace("{user.mention}", m.getMention()));
		
		builder.buildAndSend();
	}
	
	public List<Member> getHistory() { return Collections.unmodifiableList(history); }
	public void clearHistory() { history.clear(); }
}
