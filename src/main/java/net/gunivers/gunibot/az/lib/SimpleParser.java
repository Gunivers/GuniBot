package net.gunivers.gunibot.az.lib;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SimpleParser
{
	private SimpleParser() {}
	
	public static LinkedList<String> parseList(String s)
	{
		LinkedList<String> l = new LinkedList<>();
		
		if (s.charAt(0) == '[' && s.charAt(s.length() -1) == ']')
		{
			s  = s.substring(1, s.length() -1).replaceAll(" ,|, ", ",");
			Matcher m = Pattern.compile("\\[.+\\]|\\{.+\\}|[^,]+").matcher(s);
			
			while (m.find())
				l.add(m.group());
		}
		else
			l.add(s);
		
		return l;
	}
}
