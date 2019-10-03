package net.gunivers.gunibot.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import net.gunivers.gunibot.core.utils.BotUtils;
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple3;

public class ReactionAddedListener extends Events<ReactionAddEvent> {
    // Tuple3<Message, Emoji, Consumer>
    private List<Tuple3<Long, Long, Consumer<ReactionAddEvent>>> list = new ArrayList<>();

    protected ReactionAddedListener() {
	super(ReactionAddEvent.class);
    }

    @Override
    protected boolean precondition(ReactionAddEvent event) {
	if (event.getUser().block().isBot())
	    return false;
	boolean out = false;

	for (Tuple3<Long, Long, Consumer<ReactionAddEvent>> tuple : list) {
	    if (tuple.value1 == event.getMessage().block().getId().asLong()) {
		if (tuple.value2 == null)
		    return true;
		else {
		    out = tuple.value2 == BotUtils.emojiToId(event.getEmoji()) ? true : out;
		}
	    }
	}

	return out;
    }

    @Override
    protected void apply(ReactionAddEvent event) {
	list.stream().filter(tuple -> event.getMessage().block().getId().asLong() == tuple.value1)
		.filter(tuple -> tuple.value2 == null || tuple.value2 == BotUtils.emojiToId(event.getEmoji()))
		.forEach(tuple -> tuple.value3.accept(event));
    }

    public void clear() {
	list.clear();
    }

    public void on(Message message, Consumer<ReactionAddEvent> action) {
	list.add(Tuple.newTuple(message.getId().asLong(), null, action));
    }

    public void on(Message message, ReactionEmoji reactionEmoji, Consumer<ReactionAddEvent> action) {
	list.add(Tuple.newTuple(message.getId().asLong(), BotUtils.emojiToId(reactionEmoji), action));
    }

    public void cancel(Message message) {
	list = list.stream().filter(tuple -> tuple.value1 != message.getId().asLong()).collect(Collectors.toList());
    }

    public void cancel(Message message, ReactionEmoji reactionEmoji) {
	list = list.stream().filter(
		tuple -> tuple.value1 != message.getId().asLong() && tuple.value2 != BotUtils.emojiToId(reactionEmoji))
		.collect(Collectors.toList());
    }
}
