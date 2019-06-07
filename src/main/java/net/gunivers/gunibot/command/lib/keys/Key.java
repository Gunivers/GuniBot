package net.gunivers.gunibot.command.lib.keys;

import org.json.JSONException;
import org.json.JSONObject;

import net.gunivers.gunibot.command.lib.Command;
import net.gunivers.gunibot.command.lib.JsonCommandFormatException;
import net.gunivers.gunibot.command.lib.nodes.Node;
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
