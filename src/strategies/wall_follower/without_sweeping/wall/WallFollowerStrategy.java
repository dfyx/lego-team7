package strategies.wall_follower.without_sweeping.wall;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import strategies.util.ChildStrategy;
import utils.Utils;

// TODO SB should work for right and left looking sensor head
public class WallFollowerStrategy extends ChildStrategy {
	// TODO SB calibrate?
	/**
	 * desired distance to wall (in cm)
	 */
	private int referenceValue = 14;

	private static final int MAX_SPEED = 1000;
	private int speed = MAX_SPEED;

	private enum HeadOn {
		RIGHT_SIDE, LEFT_SIDE
	}

	private static HeadOn headOn;

	private static final int LINEAR_FACTOR_MOVE_AWAY = 55;
	private static final int LINEAR_FACTOR_MOVE_TOWARDS = 37;

	/**
	 * distance to wall (in cm)
	 */
	private int actualValue;

	public WallFollowerStrategy() {

	}

	protected void doInit() {
	}

	public void work() {
		// TODO wait while sensor head is moving
		actualValue = HEAD.getDistance();
		

		int direction = getMotorDirection();

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
	private int getMotorDirection() {
		int diff = (actualValue - referenceValue);
		int linearValue = 0;
		// move to wall
		if (diff > 0)
			linearValue = diff * LINEAR_FACTOR_MOVE_TOWARDS;
		else
			linearValue = diff * LINEAR_FACTOR_MOVE_AWAY;

		return Utils.clamp(linearValue, -1000, 1000);
	}

	@Override
	public boolean willStart() {
		return true;
	}

	@Override
	public boolean isStopped() {
		return false;
	}

	@Override
	public void check() {
		return;
	}
}
