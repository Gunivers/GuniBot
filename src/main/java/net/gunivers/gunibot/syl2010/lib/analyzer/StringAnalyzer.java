package net.gunivers.gunibot.syl2010.lib.analyzer;

import java.util.ArrayList;

public class StringAnalyzer extends ElementList<String> {

	private static final long serialVersionUID = 1L;

	public StringAnalyzer(String[] args) {
		super(args);
	}

	public StringAnalyzer(StringAnalyzer analyzer) {
		super(analyzer);
	}

	/**
	 * Renvoit tout les éléments (concaténés avec un espace)
	 * @return tout les éléments concaténés
	 */
	public String nextAll() {
		return nextAll(" ");
	}

	/**
	 * Renvoit tout les éléments (concaténé avec le sépérateur)
	 * @param separator le séparateur entre éléments concaténés
	 * @return tout les éléments concaténés
	 */
	public String nextAll(String separator) {
		ArrayList<String> compute = new ArrayList<>(size() - getPointer() - 1);

		while (hasNext()) {
			compute.add(next());
		}

		return String.join(separator, compute);
	}

}
