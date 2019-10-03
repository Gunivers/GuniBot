package net.gunivers.gunibot.core.lib;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gunivers.gunibot.core.utils.tuple.Tuple;
import net.gunivers.gunibot.core.utils.tuple.Tuple2;

public final class SimpleParser {
    public static final Pattern QUOTE_GROUP = Pattern.compile("\"([^\"]|\\\")*\"");
    public static final Pattern LIST_GROUP = Pattern.compile(QUOTE_GROUP.pattern() + "|\\[.+\\]|\\{.+\\}|[^,]+");

    public static final Pattern COUPLE_KEY = Pattern.compile(QUOTE_GROUP.pattern() + "|\\[.+\\]|\\{.+\\}|[^,=]+");
    public static final Pattern COUPLE = Pattern.compile(COUPLE_KEY.pattern() + '=' + LIST_GROUP.pattern());

    private SimpleParser() {
    }

    /**
     * Parse a list of the form: '{@code [value, value, ..., value]}' or
     * '{@code value}'
     */
    public static LinkedList<String> parseList(String string) {
	LinkedList<String> list = new LinkedList<>();

	if (string.charAt(0) == '[' && string.charAt(string.length() - 1) == ']') {
	    string = string.substring(1, string.length() - 1);
	    Matcher m = LIST_GROUP.matcher(string);

	    while (m.find()) {
		String group = m.group().trim();
		list.add(QUOTE_GROUP.matcher(group).matches() ? group.substring(1, group.length() - 1) : group);
	    }
	} else {
	    list.add(string);
	}

	return list;
    }

    /**
     * Parse a map of the form:
     * '<code>{key = value, key = value, ..., key = value}</code>' or
     * '{@code key = value}'
     */
    public static LinkedHashMap<String, String> parseMap(String string) {
	LinkedHashMap<String, String> map = new LinkedHashMap<>();

	if (string.charAt(0) == '{' && string.charAt(string.length() - 1) == '}') {
	    string = string.substring(1, string.length() - 1);
	    Matcher m = COUPLE.matcher(string);

	    while (m.find()) {
		Tuple2<String, String> couple = parseCouple(m.group().trim());
		map.put(couple.value1, couple.value2);
	    }
	} else if (COUPLE.matcher(string).matches()) {
	    Tuple2<String, String> couple = parseCouple(string);
	    map.put(couple.value1, couple.value2);
	}

	return map;
    }

    /**
     * Parse a key/value couple of the form: '{@code key = value}'
     */
    public static Tuple2<String, String> parseCouple(String string) {
	Matcher keyMatcher = COUPLE_KEY.matcher(string);
	keyMatcher.find();
	String key = keyMatcher.group();

	Matcher valueMatcher = LIST_GROUP.matcher(string.substring(key.length()));
	valueMatcher.find();
	String value = valueMatcher.group();

	if (QUOTE_GROUP.matcher(key.trim()).matches()) {
	    key = key.trim().substring(1, key.length() - 1);
	}
	if (QUOTE_GROUP.matcher(value.trim()).matches()) {
	    value = value.trim().substring(1, value.length() - 1);
	}

	return Tuple.newTuple(key.trim(), value.trim());
    }
}
