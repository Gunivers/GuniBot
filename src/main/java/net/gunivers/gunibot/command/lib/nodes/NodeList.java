package net.gunivers.gunibot.command.lib.nodes;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class NodeList<T> extends Node {

	private Set<T> elements = new HashSet<>();
	private BiPredicate<String, Set<T>> predicat = null;

	public NodeList(T al, BiPredicate<String, Set<T>> predicate) {
		elements.add(al);
		predicat = predicate;
	}

	public void addElements(List<T> args) {
		elements.addAll(args);
	}

	public List<T> getElements() {
		return new LinkedList<T>(elements);
	}

	@Override
	protected boolean matchesNode(String s) {
		return predicat.test(s, elements);
	}
	
	@Override
	public String toString() {
		if(elements.size() > 1)
			return "(" + elements.stream().map(s -> s.toString()).collect(Collectors.joining("|")) + ")" + childrenToString();
		return elements.iterator().next().toString() + childrenToString();
	}
}
