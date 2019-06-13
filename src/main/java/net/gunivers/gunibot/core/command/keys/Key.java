package net.gunivers.gunibot.core.command.keys;

import org.json.JSONException;
import org.json.JSONObject;

import net.gunivers.gunibot.core.command.Command;
import net.gunivers.gunibot.core.command.JsonCommandFormatException;
import net.gunivers.gunibot.core.command.nodes.Node;
import net.gunivers.gunibot.utils.tuple.Tuple2;

public abstract class Key {

	public abstract String getKey();
	
	public boolean isMandatory() {
		return false;
	}
	
	public Tuple2<String, Node> parseJson(JSONObject obj, Node n, Command c) throws JsonCommandFormatException {
		try {
			return Tuple2.newTuple(getKey(), parse(obj, n, c));
		} catch(JSONException e) {
			if(isMandatory())
				throw new JsonCommandFormatException("Clé " + getKey() + " manquante dans\n\tat " + c.getSyntaxFile());
			else
				return Tuple2.newTuple(null, n);
		}
	}
	
	protected abstract Node parse(JSONObject obj, Node n, Command c) throws JsonCommandFormatException;

}
