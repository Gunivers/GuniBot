package net.gunivers.gunibot.custom_channel;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

import discord4j.core.object.entity.Member;

public class Invitation
{
	public static final HashMap<Long, HashMap<Long, Invitation>> MAP = new HashMap<>();
	public static final Duration EXPIRATION_TIME = Duration.ofDays(2);
	
	private final Member sender;
	private final Member recipient;
	private final Instant expiration;
	
	private Invitation(Member sender, Member recipient)
	{
		this.sender = sender;
		this.recipient = recipient;
		this.expiration = Instant.ofEpochSecond(Instant.now().getEpochSecond() + EXPIRATION_TIME.getSeconds());
	}
	
	public Member getSender() { return sender; }
	public Member getRecipient() { return recipient; }
	public Instant getExpiration() { return expiration; }

	public static Invitation getInstance(Member sender, Member recipient)
	{
		MAP.putIfAbsent(sender.getId().asLong(), new HashMap<>());
		if (MAP.get(sender.getId().asLong()).containsKey(recipient.getId().asLong()))
			return MAP.get(sender.getId().asLong()).get(recipient.getId().asLong());
		
		return new Invitation(sender, recipient);
	}
}
