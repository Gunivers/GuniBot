package net.gunivers.gunibot.command.commands.administrator;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.GuildChannel;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.object.util.Snowflake;
import net.gunivers.gunibot.Main;
import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.utils.BotUtils;
import net.gunivers.gunibot.datas.serialize.Restorable;
import net.gunivers.gunibot.datas.serialize.Serializer;
import net.gunivers.gunibot.event.Events;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;
import net.gunivers.gunibot.utils.tuple.Tuple5;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class WormHoleCommand extends Command {
	
	private static class Memory implements Restorable {
		private HashMap<Tuple2<Snowflake, Snowflake>, Set<Tuple2<Snowflake, Snowflake>>> linkedChannels = new HashMap<>();

		@Override
		public Serializer save() {
			Serializer s = new Serializer();
			s.put("wormhole", linkedChannels);
			return s;
		}
		@SuppressWarnings("unchecked")
		@Override
		public void load(Serializer serializer) {
			linkedChannels = (HashMap<Tuple2<Snowflake, Snowflake>, Set<Tuple2<Snowflake, Snowflake>>>)serializer.get("wormhole");
			if(linkedChannels == null)
				linkedChannels = new HashMap<>();
		}
	}
	
	private final static int TIME = 60000;
	//Stock les les channels associés et leur Guild respectif
	private Tuple5<GuildChannel, GuildChannel, Snowflake, Message, Disposable> buffer;
	private Memory memory;
	
	{
		memory = new Memory();
		System.out.println(Main.getBotInstance());
		dataCenter.registerSystem("wormhole", memory);
	}

	
	public void link(MessageCreateEvent e, List<String> args) {
		Flux<GuildChannel> channels = Main.getBotInstance().getBotClient()
		.getGuilds()
		.flatMap(g -> g.getChannels()
				.filter(c -> 
				c instanceof MessageChannel && (c.getId().asString().equals(args.get(0)) || c.getId().asString().equals(args.get(1)))));
		if(channels.count().block() == 2) {
			
			GuildChannel channel1 = channels.blockFirst().getId().asString().equals(args.get(0)) ? channels.blockFirst() : channels.blockLast();
			GuildChannel channel2 = channels.blockLast().getId().asString().equals(args.get(1)) ? channels.blockLast() : channels.blockFirst();
			
			Mono<Message> msg = e.getMessage().getChannel().block().createMessage("Confirm link between " 
				+ channel1.getMention() + " in " + channel1.getGuild().block().getName()
				+ " and " + channel2.getMention() + " in " + channel2.getGuild().block().getName() + "?");
			Message m = msg.block();
			m.addReaction(ReactionEmoji.unicode("👍")).subscribe();
			Events.REACTION_ADDED.on(m, ReactionEmoji.unicode("👍"), this::onThumbEmojiAdded);
			Disposable disp = Mono.just(m).delayElement(Duration.ofMillis(TIME)).subscribe(ms -> ms.delete().subscribe());
			buffer = Tuple.newTuple(channel1, channel2, e.getMember().get().getId(), m, disp);
		} else
			e.getMessage().getChannel().block().createMessage("Specified channels not valid!").subscribe();
	}
	
	private void onThumbEmojiAdded(ReactionAddEvent e) {
		if(buffer != null)
			if(e.getUserId().equals(buffer._3)) {
				Events.REACTION_ADDED.cancel(e.getMessage().block());
				buffer._5.dispose();
				buffer._4.delete().subscribe();
				e.getMessage().block().getChannel().block().createMessage("Specified channels linked!").subscribe();
				Set<Tuple2<Snowflake, Snowflake>> values = memory.linkedChannels.getOrDefault(Tuple.newTuple(buffer._1.getId(), buffer._1.getGuildId()), new HashSet<>());
				values.add(Tuple.newTuple(buffer._2.getId(), buffer._2.getGuildId()));
				memory.linkedChannels.put(Tuple.newTuple(buffer._1.getId(), buffer._1.getGuildId()), values);
				e.getClient().getEventDispatcher().on(MessageCreateEvent.class).filter(mce ->  
				mce.getMessage()
				.getChannelId()
				.equals(buffer._1.getId()));
				Snowflake sf = buffer._1.getId();
				e.getClient().getEventDispatcher().on(MessageCreateEvent.class).filter(mce ->  
					mce.getMessage()
					.getChannelId()
					.equals(sf))
				.subscribe(e2 -> copyMessage(e2));				
				buffer = null;
			}
	}
	
	private void copyMessage(MessageCreateEvent e) {
		Set<Tuple2<Snowflake, Snowflake>> values = memory.linkedChannels.get(memory.linkedChannels.keySet().stream().filter(t -> t.equals(Tuple.newTuple(e.getMessage().getChannelId(), e.getGuildId().get()))).findFirst().get());
		values.forEach(channel -> {
			if(BotUtils.returnOptional(e.getClient().getGuildById(channel._2).block().getChannelById(channel._1)).isPresent() && e.getMessage().getContent().isPresent())
				BotUtils.sendMessageWithIdentity(e.getMember().get(), (MessageChannel) e.getClient().getGuildById(channel._2).block().getChannelById(channel._1).block(), e.getMessage().getContent().get());
		});
	}
	
	@Override
	public String getSyntaxFile() {
		return "administrator/wormhole.json";
	}

}
