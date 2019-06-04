package net.gunivers.gunibot.command.lib.nodes;

import net.gunivers.gunibot.command.lib.JsonCommandFormatException;

public class NodeInt extends TypeNode {

	private int min;
	private int max;

	@Override
	protected boolean matchesNode(String s) {
		try {
			int x = Integer.parseInt(s);
			return x >= min || x <= max;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@Override
	public void parse(String s) throws JsonCommandFormatException {
		min = Integer.MIN_VALUE;
		max = Integer.MAX_VALUE;
		if (s.matches("\\d+..")) {
			min = Integer.parseInt(s.replaceAll("\\.", ""));
			max = Integer.MAX_VALUE;
		} else if (s.matches("..\\d+")) {
			max = Integer.parseInt(s.replaceAll("\\.", ""));
		} else if (s.matches("\\d+..\\d+")) {
			String[] b = s.split("\\.\\.");
			min = Integer.parseInt(b[0]);
			max = Integer.parseInt(b[1]);
		} else
			throw new JsonCommandFormatException("Le couple (matches, " + s + ") ne corresponds pas au type integer");
	}
}