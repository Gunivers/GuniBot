package net.gunivers.gunibot.core.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.gunivers.gunibot.Main;

import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Member;

public class FirstConnectionListener extends Events<MemberJoinEvent>
{
	protected FirstConnectionListener() { super(MemberJoinEvent.class); }

	private final ArrayList<Member> history = new ArrayList<>();

	@Override protected boolean precondition(MemberJoinEvent event) { return true; }

	@Override
	protected void apply(MemberJoinEvent event) {
		Main.getBotInstance().getDataCenter().getDataGuild(event.getGuild().block()).getWelcomeSystem().welcome(event.getMember()); }

	public List<Member> getHistory() { return Collections.unmodifiableList(this.history); }
	public void clearHistory() { this.history.clear(); }
}
