package net.gunivers.gunibot.core.system;

public abstract class AbstractSystem implements System {

	private boolean enable = false;

	@Override
	public void enable() {
		if(!enable) enable = true;
		else throw new IllegalStateException("This system is already enable");
	}

	@Override
	public void disable() {
		if(enable) enable = false;
		else throw new IllegalStateException("This system is already disable");
	}

	@Override
	public boolean isEnabled() {
		return enable;
	}

}
