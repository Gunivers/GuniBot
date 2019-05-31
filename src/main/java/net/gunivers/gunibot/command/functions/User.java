package net.gunivers.gunibot.command.functions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.gunivers.gunibot.command.lib.Function;

public class User extends Function {

	public User getUserByName() {
		return null;
	}

	@Override
	public Set<String> getFunctionFiles() {
		return new HashSet<String>(Arrays.asList("user"));
	}

}
