package net.gunivers.gunibot.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple3;

public class ReactionAddedListener extends Events<ReactionAddEvent>
{
	private List<Tuple3<Message, ReactionEmoji, Consumer<ReactionAddEvent>>> list = new ArrayList<>();

	protected ReactionAddedListener() { super(ReactionAddEvent.class); }

	@Override
	protected boolean precondition(ReactionAddEvent event)
	{
		for (Tuple3<Message, ReactionEmoji, Consumer<ReactionAddEvent>> tuple : list)
		{
			if (tuple._1 == event.getMessage().block())
			{
				if (tuple._2 == null) return true;
				else return tuple._2 == event.getEmoji();
			}
		}
		
		return false;
	}

	@Override
	protected void apply(ReactionAddEvent event)
	{
		list.stream().filter(tuple -> event.getMessage().block().equals(tuple._1)).forEach(tuple -> tuple._3.accept(event));
	}
	
	public void clear() { list.clear(); }
	
	public void on(Message msg, Consumer<ReactionAddEvent> action) { list.add(Tuple.newTuple(msg, null, action)); }
	public void on(Message msg, ReactionEmoji r, Consumer<ReactionAddEvent> action) { list.add(Tuple.newTuple(msg, r, action)); }
	
	public void cancel(Message msg) { list = list.stream().filter(t -> t._1 != msg).collect(Collectors.toList()); }
	public void cancel(Message msg, ReactionEmoji r) { list = list.stream().filter(t -> t._1 != msg && t._2 != r).collect(Collectors.toList()); }
}
