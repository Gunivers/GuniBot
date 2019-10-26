package net.gunivers.gunibot.core.lib.parsing.commons;

import net.gunivers.gunibot.core.lib.parsing.Parser;
import net.gunivers.gunibot.core.lib.parsing.ParsingException;

public class StringParser implements Parser<String>
{
	private final String regex;

	public StringParser() { this(".*"); }
	public StringParser(String regex) { this.regex = regex; }

	@Override
	public String parse(String input) throws ParsingException
	{
		if (!input.matches(this.regex))
			throw new ParsingException("The character chain '"+ input +"' should matche the following regular expression:\n"+ this.regex);

		return input;
	}
}
