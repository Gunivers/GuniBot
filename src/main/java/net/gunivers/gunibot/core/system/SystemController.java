package net.gunivers.gunibot.core.system;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class SystemController implements RegisterSystem {

	public HashMap<String,System> systemList;

	public SystemController() {
		systemList = new HashMap<>();
	}

	@Override
	public void registerSystem(String id, System system) {
		systemList.put(id, system);
	}

	@Override
	public boolean isRegistered(String id) {
		return systemList.containsKey(id);
	}

	@Override
	public void unregisterSystem(String id) {
		systemList.remove(id);
	}

	public System getRegisteredSystem(String id) {
		return systemList.get(id);
	}

	public void enableSystem(String id) {
		if(!systemList.containsKey(id)) throw new NoSuchElementException(String.format("No system '%s' registered!", id));
		else systemList.get(id).enable();
	}

	public void isEnableSystem(String id) {
		if(!systemList.containsKey(id)) throw new NoSuchElementException(String.format("No system '%s' registered!", id));
		else systemList.get(id).isEnabled();
	}

	public void disableSystem(String id) {
		if(!systemList.containsKey(id)) throw new NoSuchElementException(String.format("No system '%s' registered!", id));
		else systemList.get(id).disable();
	}

}
