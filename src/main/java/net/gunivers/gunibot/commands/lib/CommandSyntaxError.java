package net.gunivers.gunibot.commands.lib;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class CommandSyntaxError {
	
	public enum SyntaxError {
		SYNTAX_SHORTER,
		SYNTAX_LONGER,
		ARG_INVALID
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
}
