package strategies.sections;

import robot.Platform;
import strategies.Strategy;

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
