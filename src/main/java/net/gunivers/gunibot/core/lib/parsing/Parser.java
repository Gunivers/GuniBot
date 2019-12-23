package net.gunivers.gunibot.core.lib.parsing;

@FunctionalInterface
public interface Parser<T>
{
	T parse(String input) throws ParsingException;
}
