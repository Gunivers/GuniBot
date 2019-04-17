package net.gunivers.gunibot.syl2010.lib.analyzer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Liste d'élément parcourable.
 * @author Syl2010
 *
 * @param <T> Le type des éléments stockés
 */
public class ElementList<T> extends ArrayList<T> {

	private static final long serialVersionUID = 1L;

	private int word;

	public ElementList(T[] args) {
		this(Arrays.asList(args));
	}

	public ElementList(Collection<T> args) {
		super(args);
		word = -1;;
	}

	public ElementList(ElementList<T> element_list) {
		super(element_list);
		word = element_list.word;
	}

	public boolean hasNext(int words) {
		return (word < (size() - words));
	}

	public boolean hasNext() {
		return hasNext(1);
	}

	public boolean hasPrevious() {
		return hasPrevious(1);
	}

	public boolean hasPrevious(int words) {
		return (word > words - 1);
	}

	public T next() {
		if (hasNext()) {
			return get(++word);
		} else {
			throw new IllegalArgumentException("There has no more word in the MessageAnalyzer");
		}
	}

	public T previous() {
		if (hasPrevious()) {
			return get(--word);
		} else {
			throw new IllegalArgumentException("There has no previous word in the MessageAnalyzer");
		}
	}

	public void reset() {
		word = -1;
	}

	public int getPointer() {
		return word;
	}

}
