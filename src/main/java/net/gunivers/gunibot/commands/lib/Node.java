package net.gunivers.gunibot.commands.lib;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


abstract class Node {
	
	private List<Node> children = new LinkedList<Node>();
	private Method run = null;
	
	public abstract boolean matches(String s);
	
	public void setChild(List<Node> nodes) {
		children.addAll(nodes);
	}
	
	public void setExecute(Method m) {
		run = m;
	}
	
	public Method getMethod() {
		return run;
	}
	
}

class NodeInt extends Node {

	private int min;
	private int max;
	
	public NodeInt(int mi, int ma) {
		min = mi;
		max = ma;
	}
	
	@Override
	public boolean matches(String s) {
		try {
			int x = Integer.parseInt(s);
			return x < min || x > max;
		} catch(NumberFormatException e) {
			return false;
		}
	}
	
}

class NodeString extends Node {
	
	private String regex;
	
	public NodeString(String reg) {
		regex = reg;
	}

	@Override
	public boolean matches(String s) {
		return s.matches(regex);
	}
	
}

class NodeRoot extends Node {
	
	private Set<String> aliases = new HashSet<>();
	
	public NodeRoot(String al) {
		aliases.add(al);
	}
	
	public void addAliases(List<String> args) {
		aliases.addAll(args);
	}
	
	public List<String> getAliases() {
		return new LinkedList<String>(aliases);
	}

	@Override
	public boolean matches(String s) {
		return aliases.contains(s);
	}
	
	
}
