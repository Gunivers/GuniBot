package net.gunivers.gunibot.core.lib.parsing.commons;

import net.gunivers.gunibot.core.lib.parsing.Parser;
import net.gunivers.gunibot.core.lib.parsing.ParsingException;

public class BooleanParser implements Parser<Boolean>
{
	@Override
	public Boolean parse(String input) throws ParsingException
	{
		input = input.toLowerCase();

		if (!input.matches("true|false"))
			throw new ParsingException("A boolean may only be true or false");

		return Boolean.parseBoolean(input);
	}
}
