package net.gunivers.gunibot.core.lib.parsing;

import java.util.List;

public class CharParser implements Parser<Character>
{
	private final List<Character> allowed;

	public CharParser() { this(null); }
	public CharParser(List<Character> allowed) { this.allowed = allowed; }

	@Override
	public Character parse(String input) throws ParsingException
	{
		if (input.length() != 1)
			throw new ParsingException("Expected a single character!");

		char c = input.charAt(0);

		if (this.allowed != null && !this.allowed.contains(c))
			throw new ParsingException("The character '"+ input +"' isn't usable! Usable characters list:"
					+ this.allowed.stream().map(String::valueOf).reduce("\n", (a,b) -> a + b));

		return c;
	}
}
