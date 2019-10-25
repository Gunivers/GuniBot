package net.gunivers.gunibot.core.lib.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListParser<T> implements Parser<List<T>>
{
	public static final Pattern QUOTED_GROUP = Pattern.compile("\"([^\"]|\\\")*\"");
	public static final Pattern GROUP = Pattern.compile(QUOTED_GROUP.pattern() + "|\\[.+\\]|\\{.+\\}|[^,]+");

	private final Parser<T> parser;

	public ListParser(Parser<T> parser) { this.parser = parser; }

	@Override
	public List<T> parse(String input) throws ParsingException
	{
		ArrayList<T> list = new ArrayList<>();

		for (String group : ListParser.rawParse(input))
			list.add(this.parser.parse(group));

		return list;
	}

	public static List<String> rawParse(String input) throws ParsingException
	{
		if (input.isEmpty()) throw new ParsingException("Could not parse list from an empty argument!");
		if (input.charAt(0) != '[') throw new ParsingException("Missing opening bracket: `[`");
		if (input.charAt(input.length() -1) != ']') throw new ParsingException("Missing closing bracket: `]`");

		input = input.substring(1, input.length() - 1);

		ArrayList<String> rawList = new ArrayList<>();
		Matcher m = GROUP.matcher(input);

		while (m.find())
		{
			String group = m.group().trim();
			rawList.add(QUOTED_GROUP.matcher(group).matches() ? group.substring(1, group.length() - 1) : group);
		}

		return rawList;
	}

	public Parser<T> getParser() { return this.parser; }
}
