package net.gunivers.gunibot.core.event;

import static net.gunivers.gunibot.core.utils.BotUtils.emojiToId;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple3;

public class ReactionRemovedListener extends Events<ReactionRemoveEvent>
{
	//Tuple3<Message, Emoji, Consumer>
	private List<Tuple3<Long, Long, Consumer<ReactionRemoveEvent>>> list = new ArrayList<>();

	protected ReactionRemovedListener() { super(ReactionRemoveEvent.class); }

	@Override
	protected boolean precondition(ReactionRemoveEvent event)
	{	
		if (event.getUser().block().isBot()) return false;
		boolean out = false;
		
		for (Tuple3<Long, Long, Consumer<ReactionRemoveEvent>> tuple : list)
		{
			if (tuple._1 == event.getMessage().block().getId().asLong())
			{
				if (tuple._2 == null) return true;
				else out = tuple._2 == emojiToId(event.getEmoji()) ? true : out;
			}
		}
		
		return out;
	}

	@Override
	protected void apply(ReactionRemoveEvent event)
	{
		list.stream().filter(t -> event.getMessage().block().getId().asLong() == t._1).filter(t -> t._2 == null || t._2 == emojiToId(event.getEmoji()))
			.forEach(t -> t._3.accept(event));
	}
	
	public void clear() { list.clear(); }
	
	public void on(Message msg, Consumer<ReactionRemoveEvent> action) { list.add(Tuple.newTuple(msg.getId().asLong(), null, action)); }
	public void on(Message msg, ReactionEmoji r, Consumer<ReactionRemoveEvent> action) { list.add(Tuple.newTuple(msg.getId().asLong(), emojiToId(r), action)); }
	
	public void cancel(Message msg) { list = list.stream().filter(t -> t._1 != msg.getId().asLong()).collect(Collectors.toList()); }
	public void cancel(Message msg, ReactionEmoji r) { list = list.stream().filter(t -> t._1 != msg.getId().asLong() && t._2 != emojiToId(r)).collect(Collectors.toList()); }
}
