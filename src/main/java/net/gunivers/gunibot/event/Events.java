package net.gunivers.gunibot.event;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;

public abstract class Events<E extends discord4j.core.event.domain.Event>
{
	private static EventDispatcher dispatcher;
	
	public static CommandIssuedListener COMMAND_ISSUED;
	
	public static ReactionAddedListener REACTION_ADDED;
	public static ReactionRemovedListener REACTION_REMOVED;
	public static ReactionRemovedAllListener REACTION_REMOVED_ALL;

	
	public static void registerEvents()
	{
		COMMAND_ISSUED = new CommandIssuedListener();
		
		REACTION_ADDED = new ReactionAddedListener();
		REACTION_REMOVED = new ReactionRemovedListener();
		REACTION_REMOVED_ALL = new ReactionRemovedAllListener();
	}
	
	
	protected E last = null;
	
	protected Events(Class<E> clazz)
	{
		dispatcher.on(clazz).filter(this::precondition).subscribe(event ->
		{
			this.last = event;
			this.apply(event);
		});
	}
	
	protected abstract boolean precondition(E event);
	protected abstract void apply(E event);
	
	public E getLastEvent() { return this.last; }
	
	public static void initialize(ReadyEvent event)
	{
		Events.dispatcher = event.getClient().getEventDispatcher();
		Events.registerEvents();
	}
}
