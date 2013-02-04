package strategies;

import robot.Platform;

/**
 * Just drive forward - for example for reading a barcode
 *
 */
public class DriveForwardStrategy extends Strategy {

	@Override
	protected void doInit() {
	}

	@Override
	protected void doRun() {
		Platform.ENGINE.move(1000);
	}
}
