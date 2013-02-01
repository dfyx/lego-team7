package strategies.wall_follower;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import strategies.Strategy;
import utils.Utils;

// TODO SB should work for right and left looking sensor head
public class WallFollowerStrategy extends Strategy {
	// TODO SB calibrate?
	/**
	 * desired distance to wall (in cm)
	 */
	private int referenceValue = 15;

	private static final int MAX_SPEED = 1000;
	private int speed = MAX_SPEED;

	/**
	 * Turn on max speed outside of +- 5cm corridor 5*_200_ = 1000
	 */
	private static final int LINEAR_FACTOR_MOVE_AWAY = 70;
	private static final int LINEAR_FACTOR_MOVE_TOWARDS = 50;

	/**
	 * distance to wall (in cm)
	 */
	private int actualValue;

	public WallFollowerStrategy() {

	}

	protected void doInit() {
	}

	protected void doRun() {
		System.out.println("follow");
	    // doInit is moving the sensor head nonblocking -> skip control loop
	    // until the sensor head arrived at its final position 
	    
		actualValue = WallFollowerController.getWallDistance();

		int direction = getMotorSpeed();

		if (headOn == HeadOn.LEFT_SIDE)
			direction = -direction;

		System.out.println("IST/SOLL: " + actualValue + " / " + referenceValue
				+ " -> " + direction);

		ENGINE.move(speed, direction);
	}

	// TODO SB doesn't work on big distances
	/**
	 * 
	 * @return positive = move to wall; negative = move away from wall
	 */
	private int getMotorSpeed() {
		int diff = (actualValue - referenceValue);
		int linearValue = 0;
		// move to wall
		if (diff > 0)
			linearValue = diff * LINEAR_FACTOR_MOVE_TOWARDS;
		else
			linearValue = diff * LINEAR_FACTOR_MOVE_AWAY;

		return Utils.clamp(linearValue, -1000, 1000);
	}
}
