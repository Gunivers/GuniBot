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

public class ReactionRemovedListener extends Events<ReactionRemoveEvent> {
    // Tuple3<Message, Emoji, Consumer>
    private List<Tuple3<Long, Long, Consumer<ReactionRemoveEvent>>> list = new ArrayList<>();

    protected ReactionRemovedListener() {
	super(ReactionRemoveEvent.class);
    }

    @Override
    protected boolean precondition(ReactionRemoveEvent event) {
	if (event.getUser().block().isBot())
	    return false;
	boolean out = false;

	for (Tuple3<Long, Long, Consumer<ReactionRemoveEvent>> tuple : list) {
	    if (tuple.value1 == event.getMessage().block().getId().asLong()) {
		if (tuple.value2 == null)
		    return true;
		else {
		    out = tuple.value2 == emojiToId(event.getEmoji()) ? true : out;
		}
	    }
	}

	return out;
    }

    @Override
    protected void apply(ReactionRemoveEvent event) {
	list.stream().filter(tuple -> event.getMessage().block().getId().asLong() == tuple.value1)
		.filter(tuple -> tuple.value2 == null || tuple.value2 == emojiToId(event.getEmoji()))
		.forEach(tuple -> tuple.value3.accept(event));
    }

    public void clear() {
	list.clear();
    }

    public void on(Message message, Consumer<ReactionRemoveEvent> action) {
	list.add(Tuple.newTuple(message.getId().asLong(), null, action));
    }

    public void on(Message message, ReactionEmoji r, Consumer<ReactionRemoveEvent> action) {
	list.add(Tuple.newTuple(message.getId().asLong(), emojiToId(r), action));
    }

    public void cancel(Message message) {
	list = list.stream().filter(t -> t.value1 != message.getId().asLong()).collect(Collectors.toList());
    }

    public void cancel(Message message, ReactionEmoji reactionEmoji) {
	list = list.stream()
		.filter(tuple -> tuple.value1 != message.getId().asLong() && tuple.value2 != emojiToId(reactionEmoji))
		.collect(Collectors.toList());
    }
}
