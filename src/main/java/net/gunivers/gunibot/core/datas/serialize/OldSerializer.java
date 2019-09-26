package net.gunivers.gunibot.core.datas.serialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class OldSerializer extends HashMap<String, Serializable> {

    private static final long serialVersionUID = 1808466229478997381L;

    public OldSerializer() {
	super();
    }

    public JSONObject toJson() {
	HashMap<String, byte[]> output = new HashMap<>();
	for (Entry<String, Serializable> entry : entrySet()) {
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    try {
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(entry.getValue());
		output.put(entry.getKey(), baos.toByteArray());
	    } catch (IOException e) {
		e.printStackTrace();
	    }

	}
	return new JSONObject(output);
    }

    @SuppressWarnings("unchecked")
    public static OldSerializer from(JSONObject json) {
	Map<String, Object> dataMap = new HashMap<>();
	for (Entry<String, Object> entry : json.toMap().entrySet()) {
	    // System.out.println("key = "+entry.getKey().toString()+" | value =
	    // "+entry.getValue().toString());
	    ArrayList<Integer> buffer = (ArrayList<Integer>) entry.getValue();
	    byte[] array = new byte[buffer.size()];
	    for (int i = 0; i < buffer.size(); ++i) {
		array[i] = (byte) (int) buffer.get(i);
	    }
	    ByteArrayInputStream bais = new ByteArrayInputStream(array);
	    try {
		ObjectInputStream ois = new ObjectInputStream(bais);
		dataMap.put(entry.getKey(), ois.readObject());
	    } catch (IOException | ClassNotFoundException e) {
		e.printStackTrace();
	    }
	}
	OldSerializer output = new OldSerializer();
	// Map<String, Object> dataMap = json.toMap();
	output.putAll((Map<? extends String, ? extends Serializable>) dataMap);
	return output;
    }
}
