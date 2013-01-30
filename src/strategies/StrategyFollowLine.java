package strategies;

import utils.Utils;
import static robot.Platform.ENGINE;

// TODO SB should work for right and left looking sensor head
public class StrategyFollowLine extends Strategy {
	// TODO SB calibrate?
	/**
	 * desired distance to wall (in mm)
	 */
	private int referenceValue = 100;

	private int speed = 1000;

	private enum HeadOn {
		Right, Left
	}

	private static HeadOn headOn;

	/**
	 * Turn on max speed outside of +- 5cm corridor 50*_20_ = 1000
	 */
	private static final int LINEAR_FACTOR = 20;

	/**
	 * distance to wall (in mm)
	 */
	private int actualValue;

	protected void doInit() {
		headOn = HeadOn.Right;
	}

	protected void doRun() {
		actualValue = 0; // TODO SB set to real sensor data

		int direction = getMotorSpeed();

		if (headOn == HeadOn.Left)
			direction = -direction;

		ENGINE.move(speed, direction);
	}

	// TODO SB doesn't work on big distances
	/**
	 * 
	 * @return positive = move to wall; negative = move away from wall
	 */
	private int getMotorSpeed() {
		int linearValue = (actualValue - referenceValue) * LINEAR_FACTOR;
		return Utils.clamp(linearValue, -1000, 1000);
	}
}
