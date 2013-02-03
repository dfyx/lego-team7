package strategies.util;

import strategies.Strategy;

public abstract class ChildStrategy extends Strategy{
	private int runCount = 0;
	public abstract boolean willStart();
	public abstract boolean isStopped();
	
	public boolean justStarted() {
		return runCount == 0;
	}
	/**
	 * Call this method, when the strategy can check if it triggers.
	 * Don't pause calling this method once you started calling it.
	 * If you need to pause, call #init.
	 */
	public abstract void check();
	/**
	 * Call only, if the strategy can control motors, etc.
	 */
	public final void doRun() {
		work();
		runCount++;
	}
	
	public abstract void work();
}
