package strategies.wall_follower.wall;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import strategies.util.ChildStrategy;
import utils.Utils;
import utils.Utils.Side;

// TODO SB should work for right and left looking sensor head
public class WallRegulatorStrategy extends ChildStrategy {
	private Side headSide;

	// TODO SB calibrate?
	/**
	 * desired distance to wall (in cm)
	 */
	private final int REFERENCE_VALUE;

	private static final int MAX_SPEED = 1000;
	private int speed = MAX_SPEED;

	private static final int LINEAR_FACTOR_MOVE_AWAY = 55;
	private static final int LINEAR_FACTOR_MOVE_TOWARDS = 37;
	
	private final int MAX_MOTOR_DIRECTION;
	
	/**
	 * used to turn collision detection off, if we are regulating too much.
	 */
	private static final int BAD_VALUE = 200;

	/**
	 * distance to wall (in cm)
	 */
	private int actualValue;

	/**
	 * 
	 * @param headSide
	 * @param maxMotorDirection Should be around 500
	 * @param desired distance to the wall. Should be around 14.
	 */
	public WallRegulatorStrategy(Side headSide, int maxMotorDirection, int desiredWallDistance) {
		this.headSide = headSide;
		MAX_MOTOR_DIRECTION = maxMotorDirection;
		REFERENCE_VALUE = desiredWallDistance;
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
		int diff = (actualValue - REFERENCE_VALUE);
		int linearValue = 0;
		// move to wall
		if (diff > 0)
			linearValue = diff * LINEAR_FACTOR_MOVE_TOWARDS;
		else
			linearValue = diff * LINEAR_FACTOR_MOVE_AWAY;

		return Utils.clamp(linearValue, -MAX_MOTOR_DIRECTION, MAX_MOTOR_DIRECTION);
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
