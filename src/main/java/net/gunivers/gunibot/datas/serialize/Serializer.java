package net.gunivers.gunibot.datas.serialize;

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

public class Serializer extends HashMap<String, Serializable> {

	private static final long serialVersionUID = 1808466229478997381L;

	public Serializer() {
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
	public static Serializer from(JSONObject json) {
		Map<String, Object> data_map = new HashMap<>();
		for(Entry<String, Object> entry : json.toMap().entrySet()) {
			ArrayList<Integer> buffer = (ArrayList<Integer>) entry.getValue();
			byte[] array = new byte[buffer.size()];
			for(int i = 0; i < buffer.size(); ++i)
				array[i] = (byte)(int)buffer.get(i);
			ByteArrayInputStream bais = new ByteArrayInputStream(array);
			try {
				ObjectInputStream ois = new ObjectInputStream(bais);
				data_map.put(entry.getKey(), ois.readObject());
			} catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		Serializer output = new Serializer();
		//Map<String, Object> data_map = json.toMap();
		output.putAll((Map<? extends String, ? extends Serializable>) data_map);
		return output;
	}
}
