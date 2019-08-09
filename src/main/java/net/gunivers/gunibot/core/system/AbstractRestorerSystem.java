package net.gunivers.gunibot.core.system;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public abstract class AbstractRestorerSystem implements RestorerSystem {

	public HashMap<String, RestorableSystem> restorableSystems;

	public AbstractRestorerSystem() {
		restorableSystems = new HashMap<>();
	}

	@Override
	public void registerSystem(String id, RestorableSystem system) {
		restorableSystems.put(id, system);
	}

	@Override
	public boolean isRegistered(String id) {
		return restorableSystems.containsKey(id);
	}

	@Override
	public void unregisterSystem(String id) {
		restorableSystems.remove(id);
	}

	@Override
	public RestorableSystem getSystem(String id) {
		if(!restorableSystems.containsKey(id)) throw new NoSuchElementException(String.format("No system '%s' registered!", id));
		else return restorableSystems.get(id);
	}

	@Override
	public abstract void saveSystem(String id);

	@Override
	public abstract void loadSystem(String id);

	@Override
	public Iterable<Entry<String, RestorableSystem>> getSystems() {
		return restorableSystems.entrySet();
	}

}
