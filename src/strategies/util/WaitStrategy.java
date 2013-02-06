package strategies.util;

import strategies.Strategy;
import utils.Utils;

public class WaitStrategy extends Strategy {
	private int waittime;
	private int endtime;

	public WaitStrategy(int waittime) {
		this.waittime = waittime;
	}

	@Override
	protected void doInit() {
		endtime = Utils.getSystemTime() + waittime;
	}

	@Override
	protected void doRun() {
		if (Utils.getSystemTime() >= endtime) {
			setFinished();
		}
	}

}
