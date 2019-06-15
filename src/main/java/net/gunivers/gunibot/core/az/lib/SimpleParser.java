package net.gunivers.gunibot.core.az.lib;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.gunivers.gunibot.utils.tuple.Tuple;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public final class SimpleParser
{	
	public static final Pattern QUOTE_GROUP = Pattern.compile("\"([^\"]|\\\")*\"");
	public static final Pattern LIST_GROUP = Pattern.compile(QUOTE_GROUP.pattern() + "|\\[.+\\]|\\{.+\\}|[^,]+");
	
	public static final Pattern COUPLE_KEY = Pattern.compile(QUOTE_GROUP.pattern() + "|\\[.+\\]|\\{.+\\}|[^,=]+");
	public static final Pattern COUPLE = Pattern.compile(COUPLE_KEY.pattern() +'='+ LIST_GROUP.pattern());
	
	private SimpleParser() {}
	
	/**
	 * Parse a list of the form: '{@code [value, value, ..., value]}' or '{@code value}'
	 */
	public static LinkedList<String> parseList(String s)
	{
		LinkedList<String> list = new LinkedList<>();
		
		if (s.charAt(0) == '[' && s.charAt(s.length() -1) == ']')
		{
			s  = s.substring(1, s.length() -1);
			Matcher m = LIST_GROUP.matcher(s);
			
			while (m.find()) {
				String group = m.group().trim();
				list.add(QUOTE_GROUP.matcher(group).matches() ? group.substring(1, group.length() -1) : group);
			}
		} else
			list.add(s);
		
		return list;
	}
	
	/**
	 * Parse a map of the form: '<code>{key = value, key = value, ..., key = value}</code>' or '{@code key = value}'
	 */
	public static LinkedHashMap<String, String> parseMap(String s)
	{
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		
		if (s.charAt(0) == '{' && s.charAt(s.length() -1) == '}')
		{
			s = s.substring(1, s.length() -1);
			Matcher m = COUPLE.matcher(s);
			
			while (m.find()) {
				Tuple2<String,String> couple = parseCouple(m.group().trim());
				map.put(couple._1, couple._2);
			}
		} else if (COUPLE.matcher(s).matches()) {
			Tuple2<String, String> couple = parseCouple(s);
			map.put(couple._1, couple._2);
		}
		
		return map;
	}
	
	/**
	 * Parse a key/value couple of the form: '{@code key = value}'
	 */
	public static Tuple2<String,String> parseCouple(String s)
	{
		Matcher keym = COUPLE_KEY.matcher(s); keym.find();
		String key = keym.group();
		
		Matcher valuem = LIST_GROUP.matcher(s.substring(key.length())); valuem.find();
		String value = valuem.group();

		if (QUOTE_GROUP.matcher(key.trim()).matches()) key = key.trim().substring(1, key.length() -1);
		if (QUOTE_GROUP.matcher(value.trim()).matches()) value = value.trim().substring(1, value.length() -1);
		
		return Tuple.newTuple(key.trim(), value.trim());
	}
}
