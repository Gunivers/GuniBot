package net.gunivers.gunibot.core.system;

import java.util.Map.Entry;

public interface RestorerSystem {

	public void registerSystem(String id, RestorableSystem system);

	public boolean isRegistered(String id);

	public void unregisterSystem(String id);

	public RestorableSystem getSystem(String id);

	public void saveSystem(String id);

	public void loadSystem(String id);

	public Iterable<Entry<String, RestorableSystem>> getSystems();

}
