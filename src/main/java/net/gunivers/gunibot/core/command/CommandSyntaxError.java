package net.gunivers.gunibot.core.command;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommandSyntaxError {
	
	public enum SyntaxError {
		SYNTAX_SHORTER("Too much arguments provided!"),
		SYNTAX_LONGER("Missing required argument(s)"),
		ARG_INVALID("Invalid argument"),
		;
		
		public final String text;
		private SyntaxError(String text) { this.text = text; }
	}

	private List<String> path = new LinkedList<>();
	private SyntaxError error = null;
	private int deep = 0;
	
	public CommandSyntaxError() {}
	
	public CommandSyntaxError(SyntaxError error, String path) {
		this(error);
		this.path.add(path);
		deep++;
	}
	
	public CommandSyntaxError(SyntaxError error, String[] path) {
		this(error);
		this.path.addAll(Arrays.asList(path));
		deep += path.length;
	}
	
	public CommandSyntaxError(SyntaxError error) {
		this.error = error;
	}
	
	public void addStringToPathFirst(String s) {
		path.add(0, s);
		deep++;
	}
	
	public void addStringAndMerge(String[] s) {
		path.addAll(Arrays.asList(s));
		deep += s.length;
	}
	
	public boolean isDeeperThan(CommandSyntaxError cse2) {
		return deep > cse2.deep;
	}

	public SyntaxError getError() {
		return error;
	}
	
	public List<String> getPath() {
		return new LinkedList<String>(path);
	}
	
	@Override
	public String toString() { return error == null ? "" : error.text; }
}
