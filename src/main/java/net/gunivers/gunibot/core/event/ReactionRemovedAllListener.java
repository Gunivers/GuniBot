package net.gunivers.gunibot.core.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import discord4j.core.event.domain.message.ReactionRemoveAllEvent;
import discord4j.core.object.entity.Message;
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;

public class ReactionRemovedAllListener extends Events<ReactionRemoveAllEvent> {
    private List<Tuple2<Long, Consumer<ReactionRemoveAllEvent>>> list = new ArrayList<>();

    protected ReactionRemovedAllListener() {
	super(ReactionRemoveAllEvent.class);
    }

    @Override
    protected boolean precondition(ReactionRemoveAllEvent event) {
	for (Tuple2<Long, Consumer<ReactionRemoveAllEvent>> tuple : list)
	    if (tuple.value1 == event.getMessage().block().getId().asLong())
		return true;

	return false;
    }

    @Override
    protected void apply(ReactionRemoveAllEvent event) {
	list.stream().filter(tuple -> event.getMessage().block().getId().asLong() == tuple.value1)
		.forEach(tuple -> tuple.value2.accept(event));
    }

    public void clear() {
	list.clear();
    }

    public void on(Message message, Consumer<ReactionRemoveAllEvent> action) {
	list.add(Tuple.newTuple(message.getId().asLong(), action));
    }

    public void cancel(Message message) {
	list = list.stream().filter(tuple -> tuple.value1 != message.getId().asLong()).collect(Collectors.toList());
    }
}
