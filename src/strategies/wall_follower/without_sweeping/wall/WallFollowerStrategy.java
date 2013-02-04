package strategies.wall_follower.without_sweeping.wall;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import strategies.util.ChildStrategy;
import utils.Utils;
import utils.Utils.Side;

// TODO SB should work for right and left looking sensor head
public class WallFollowerStrategy extends ChildStrategy {
	private Side headSide;

	// TODO SB calibrate?
	/**
	 * desired distance to wall (in cm)
	 */
	private int referenceValue = 14;

	private static final int MAX_SPEED = 1000;
	private int speed = MAX_SPEED;

	private static final int LINEAR_FACTOR_MOVE_AWAY = 55;
	private static final int LINEAR_FACTOR_MOVE_TOWARDS = 37;
	
	/**
	 * used to turn collision detection off, if we are regulating too much.
	 */
	private static final int BAD_VALUE = 200;

	/**
	 * distance to wall (in cm)
	 */
	private int actualValue;

	public WallFollowerStrategy(Side headSide) {
		this.headSide = headSide;
	}

	protected void childInit() {
	}

	public void work() {
		// TODO wait while sensor head is moving
		actualValue = HEAD.getDistance();

		int direction = getMotorDirection();

		if (headSide == Side.LEFT)
			direction = -direction;

		// System.out.println("IST/SOLL: " + actualValue + " / " +
		// referenceValue
		// + " -> " + direction);

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

	/**
	 * @return true, iff we do not drive on a straight line.
	 */
	public boolean badValues() {
		int abs = getMotorDirection();
		if (abs < 0)
			abs *= -1;
		return abs > BAD_VALUE;
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
