package strategies.wall_follower;

import static robot.Platform.ENGINE;
import lejos.nxt.Motor;
import strategies.Strategy;

/**
 * Used to adjust the robot at the end of a wall
 * @author sebastian
 *
 */
public class QuarterCircleStrategy extends Strategy {
	/**
	 * count of the motor, when the robot looses the wall
	 */
	int startTachoCount;
	/**
	 * count of the motor, when we want to start the curve
	 */
	int endTachoCount;
	
	
	// Regelparameter
	/**
	 * In tacho count
	 */
	private static int DESIRED_ROTATION_DISTANCE = 150;
	
	// END Regelparameter
	
	/**
	 * The motor speed
	 */
	private static int MAX_SPEED = 1000;
	private static int MOVE_ROTATION = -425;//-375
	
	// TODO SB also for drive right
	/**
	 * Rotation speed
	 */
	private static int ROTATION_SPEED = 1000;
	private static int ROTATION_DIRECTION = 1000;
	
	private boolean hasTurned;
	
	// TODO SB make this work with sensor on both sides
	@Override
	protected void doInit() {
		startTachoCount = Motor.A.getTachoCount();
		endTachoCount = startTachoCount + DESIRED_ROTATION_DISTANCE;
		hasTurned = false;
	}
	
	private boolean shouldTurn() {
		return !hasTurned && Motor.A.getTachoCount() < endTachoCount;
	}

	@Override
	protected void doRun() {
		if(shouldTurn()) {
			System.out.println("turn");
			ENGINE.move(ROTATION_SPEED, ROTATION_DIRECTION);
		} else {
			System.out.println("move");
			hasTurned = true;
			ENGINE.move(-MAX_SPEED, MOVE_ROTATION);
		}
	}
	
}
