package net.gunivers.gunibot.command.lib.nodes;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NodeRoot extends Node {

	private Set<String> aliases = new HashSet<>();

	public NodeRoot(String al) {
		aliases.add(al);
	}

	public void addAliases(List<String> args) {
		aliases.addAll(args);
	}

	public List<String> getElements() {
		return new LinkedList<>(aliases);
	}

	@Override
	protected boolean matchesNode(String s) {
		return aliases.contains(s);
	}
	
	@Override
	public String toString() {
		if(aliases.size() > 1)
			return "(" + aliases.stream().map(s -> s).collect(Collectors.joining("|")) + ")" + childrenToString();
		return aliases.iterator().next().toString() + childrenToString();
	}
}
