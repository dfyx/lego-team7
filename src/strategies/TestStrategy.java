package strategies;

import robot.Platform;

public class TestStrategy extends Strategy {

	protected void doInit() {
		Platform.HEAD.startCheckStalled(false);
	}

	protected void doRun() {
		if(Platform.HEAD.isStalled()) {
			System.out.println("isStalled");
			Platform.HEAD.stopMoving();
		}
	}
}
