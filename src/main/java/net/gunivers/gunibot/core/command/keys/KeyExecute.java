package net.gunivers.gunibot.core.command.keys;

import java.lang.reflect.Method;

import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;

public class KeyExecute extends Key {

	private static KeyExecute instance;

	private KeyExecute() {}
	
	public static KeyExecute getInstance() {
		return instance == null ? instance = new KeyExecute() : instance;
	}

	@Override
	public String getKey() {
		return "execute";
	}

	@Override
	protected Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		String methodName = obj.getString(getKey());
		Method execute = getMethodByName(methodName, c);
		if (execute == null)
			throw new JsonCommandFormatException("La fonction " + methodName + " n'existe pas dans la classe "
					+ c.getClass().getSimpleName());
		n.setExecute(execute);
		return n;
	}
	
	
	private Method getMethodByName(String name, Command command) {
		Method[] methods = command.getClass().getMethods();
		for (Method method : methods)
			if (method.getName().equals(name))
				return method;
		return null;
	}

}
