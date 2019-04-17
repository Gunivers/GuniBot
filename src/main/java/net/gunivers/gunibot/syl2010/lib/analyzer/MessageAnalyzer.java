package net.gunivers.gunibot.syl2010.lib.analyzer;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import discord4j.core.DiscordClient;
import discord4j.core.object.Embed;
import discord4j.core.object.entity.Attachment;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.Message.Type;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.Webhook;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class MessageAnalyzer extends StringAnalyzer {

	private static final long serialVersionUID = 1L;

	private Message message;
	private int word;

	public MessageAnalyzer(Message message) {
		super(message.getContent().get().replaceAll("\\h+", " ").trim().split(" "));
		this.message = message;
		word = -1;
	}

	public MessageAnalyzer(MessageAnalyzer analyzer) {
		super(analyzer);
		message = analyzer.message;
		word = analyzer.word;
	}

	public Message getMessage() {
		return message;
	}

	public DiscordClient getClient() {
		return message.getClient();
	}

	public Optional<User> getAuthor() {
		return message.getAuthor();
	}

	public Mono<Member> getAuthorAsMember() {
		return message.getAuthorAsMember();
	}

	public Mono<MessageChannel> getChannel() {
		return message.getChannel();
	}

	public Snowflake getChannelId() {
		return message.getChannelId();
	}

	public Mono<Guild> getGuild() {
		return message.getGuild();
	}

	public Instant getTimestamp() {
		return message.getTimestamp();
	}

	public Optional<Instant> getEditedTimestamp() {
		return message.getEditedTimestamp();
	}

	public Set<Attachment> getAttachments() {
		return message.getAttachments();
	}

	public List<Embed> getEmbeds() {
		return message.getEmbeds();
	}

	public Snowflake getId(){
		return message.getId();
	}

	public Type getType() {
		return message.getType();
	}

	public Mono<Webhook> getWebhook() {
		return message.getWebhook();
	}

	public Optional<Snowflake> getWebhookId() {
		return message.getWebhookId();
	}

	public Set<Reaction> getReactions(){
		return message.getReactions();
	}

	public Flux<User> getReactors(ReactionEmoji emoji){
		return message.getReactors(emoji);
	}
}
