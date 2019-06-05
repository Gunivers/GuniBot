package net.gunivers.gunibot.command.lib.nodes.interfaces;

public interface Gettable<T> extends Matchable
{
	public T getFrom(String s);
}
