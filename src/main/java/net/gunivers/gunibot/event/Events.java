package net.gunivers.gunibot.event;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.lifecycle.ReadyEvent;

/**
 * This class intends to manage all general events. For instance, a listener should be created when the event it manages has the same behavior
 * in <b>any</b> context.
 * <p>
 * In order to create a listener, create a class {@code EventNamedListener extends Events<EventNameEvent>}, then create a public static field
 * of name EVENT_NAMED and create its instance in the method registerEvents
 * 
 * @author A~Z
 *
 * @param <E> the "father" event
 */
public abstract class Events<E extends discord4j.core.event.domain.Event>
{
	private static EventDispatcher dispatcher;
	
	public static CommandIssuedListener COMMAND_ISSUED;
	public static FirstConnectionListener FIRST_CONNECTED;
	
	public static ReactionAddedListener REACTION_ADDED;
	public static ReactionRemovedListener REACTION_REMOVED;
	public static ReactionRemovedAllListener REACTION_REMOVED_ALL;	
	
	protected E last = null;

	protected Events(Class<E> clazz)
	{
		dispatcher.on(clazz).filter(this::precondition).subscribe(event ->
		{
			this.last = event;
			this.apply(event);
		});
	}

	/**
	 * 
	 * @param event the event issued by discord
	 * @return true if the event is of the right type. Event with no effect should not pass this
	 */
	protected abstract boolean precondition(E event);
	protected abstract void apply(E event);
	
	public E getLastEvent() { return this.last; }
	
	public static void initialize(ReadyEvent event)
	{
		Events.dispatcher = event.getClient().getEventDispatcher();
		Events.registerEvents();
	}
	
	public static void registerEvents()
	{
		System.out.println("Registering Events...");
		
		COMMAND_ISSUED = new CommandIssuedListener();
		FIRST_CONNECTED = new FirstConnectionListener();
		
		REACTION_ADDED = new ReactionAddedListener();
		REACTION_REMOVED = new ReactionRemovedListener();
		REACTION_REMOVED_ALL = new ReactionRemovedAllListener();
		
		System.out.println("Events registered!");
	}
	
	public static EventDispatcher getDispatcher() { return dispatcher; }
}
