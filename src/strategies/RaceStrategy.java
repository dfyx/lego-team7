package strategies;

import robot.Platform;

//TODO IMPLEMENT!!!

public class RaceStrategy extends Strategy {

	@Override
	protected void doInit() {
	}

	@Override
	protected void doRun() {
		Platform.ENGINE.move(1000);
	}
}
