package net.gunivers.gunibot.core.system;

public interface RegisterSystem {

	public void registerSystem(String id, System system);

	public boolean isRegistered(String id);

	public void unregisterSystem(String id);

}
