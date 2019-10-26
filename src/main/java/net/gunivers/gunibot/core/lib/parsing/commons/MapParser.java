package net.gunivers.gunibot.core.lib.parsing.commons;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gunivers.gunibot.core.lib.parsing.Parser;
import net.gunivers.gunibot.core.lib.parsing.ParsingException;
import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;

public class MapParser<K,V> implements Parser<Map<K,V>>
{
	public static final Pattern QUOTED_GROUP = ListParser.QUOTED_GROUP;
	public static final Pattern GROUP = ListParser.GROUP;
	public static final Pattern KEY = Pattern.compile(QUOTED_GROUP.pattern() + "|\\[.+\\]|\\{.+\\}|[^,=]+");
	public static final Pattern COUPLE = Pattern.compile(KEY.pattern() + '=' + GROUP.pattern());

	private final Parser<K> keyParser;
	private final Parser<V> valueParser;

	public MapParser(Parser<K> keyParser, Parser<V> valueParser)
	{
		this.keyParser = keyParser;
		this.valueParser = valueParser;
	}

	@Override
	public Map<K, V> parse(String input) throws ParsingException
	{
		Map<String, String> rawMap = MapParser.rawParse(input);
		Map<K, V> map = new HashMap<>();

		for (Entry<String, String> couple : rawMap.entrySet())
			map.putIfAbsent(this.keyParser.parse(couple.getKey()), this.valueParser.parse(couple.getValue()));

		return map;
	}

	public static Map<String, String> rawParse(String input) throws ParsingException
	{
		if (input.isEmpty()) throw new ParsingException("Could not parse map from an empty argument!");
		if (input.charAt(0) != '{') throw new ParsingException("Missing opening bracket: `{`");
		if (input.charAt(input.length() -1) != '}') throw new ParsingException("Missing closing bracket: `}`");

		input = input.substring(1, input.length() - 1);

		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		Matcher m = COUPLE.matcher(input);

		while (m.find())
		{
			Tuple2<String, String> couple = MapParser.parseCouple(m.group().trim());
			map.put(couple.value1, couple.value2);
		}

		return map;
	}

	public static Tuple2<String, String> parseCouple(String input)
	{
		Matcher keyMatcher = KEY.matcher(input);
		keyMatcher.find();
		String key = keyMatcher.group();

		Matcher valueMatcher = GROUP.matcher(input.substring(key.length()));
		valueMatcher.find();
		String value = valueMatcher.group();

		if (QUOTED_GROUP.matcher(key.trim()).matches())
			key = key.trim().substring(1, key.length() - 1);

		if (QUOTED_GROUP.matcher(value.trim()).matches())
			value = value.trim().substring(1, value.length() - 1);

		return Tuple.newTuple(key.trim(), value.trim());
	}

	public Parser<K> getKeyParser() { return this.keyParser; }
	public Parser<V> getValueParser() { return this.valueParser; }
}
