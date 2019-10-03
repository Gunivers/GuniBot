package net.gunivers.gunibot.core.system;

import java.util.Map.Entry;
import java.util.NoSuchElementException;

public class SystemControllerV2 extends SystemController {

    public RestorerSystem restorer;

    public SystemControllerV2() {
    }

    public void submitRestorer(RestorerSystem newRestorer) {
	if (restorer == null) {
	    restorer = newRestorer;
	} else {
	    for (Entry<String, RestorableSystem> entry : restorer.getSystems()) {
		newRestorer.registerSystem(entry.getKey(), entry.getValue());
	    }
	    restorer = newRestorer;
	}
    }

    public void registerSystem(String id, RestorableSystem system) {
	restorer.registerSystem(id, system);
	systemList.put(id, system);
    }

    @Override
    public void registerSystem(String id, System system) {
	systemList.put(id, system);
    }

    public void saveSystem(String id) {
	if (restorer == null)
	    throw new NullPointerException("No restorer has been submit!");
	else if (!this.isRegistered(id))
	    throw new NoSuchElementException(String.format("No system '%s' registered!", id));
	else if (!restorer.isRegistered(id))
	    throw new NoSuchElementException(String.format("The system '%s' is not a restorable system!", id));
	else {
	    restorer.saveSystem(id);
	}
    }

    public void loadSystem(String id) {
	if (restorer == null)
	    throw new NullPointerException("No restorer has been submit!");
	else if (!this.isRegistered(id))
	    throw new NoSuchElementException(String.format("No system '%s' registered!", id));
	else if (!restorer.isRegistered(id))
	    throw new NoSuchElementException(String.format("The system '%s' is not a restorable system!", id));
	else {
	    restorer.saveSystem(id);
	}
    }

    public void saveAll() {
	if (restorer == null)
	    throw new NullPointerException("No restorer has been submit!");
	else {
	    systemList.keySet().stream().filter(id -> restorer.isRegistered(id)).forEach(id -> restorer.saveSystem(id));
	}
    }

    public void loadAll() {
	if (restorer == null)
	    throw new NullPointerException("No restorer has been submit!");
	else {
	    systemList.keySet().stream().filter(id -> restorer.isRegistered(id)).forEach(id -> restorer.loadSystem(id));
	}
    }

}
