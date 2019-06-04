package net.gunivers.gunibot.command.lib;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;

import net.gunivers.gunibot.command.lib.nodes.Node;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public abstract class Function {
	
	public static final Map<String, Tuple2<Node, Function>> functions = new HashMap<>();
	

	public static void loadFunctions() {
		Reflections reflections = new Reflections("net.gunivers.gunibot.command.functions");
		 Set<Class<? extends Function>> allCommands = reflections.getSubTypesOf(Function.class);
		 allCommands.forEach(cmd -> {
			try {
				if(!cmd.isAnnotationPresent(Ignore.class)) {
					Function f = cmd.newInstance();
//					FunctionsParser.parseFunction(f);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
	}
	
	public abstract Set<String> getFunctionFiles();
	
}
