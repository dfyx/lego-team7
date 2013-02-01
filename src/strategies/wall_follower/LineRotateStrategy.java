package strategies.wall_follower;

import static robot.Platform.ENGINE;
import lejos.nxt.Motor;
import strategies.Strategy;


// TODO SB remove?

/**
 * Used to adjust the robot at the end of a wall
 * @author sebastian
 *
 */
public class LineRotateStrategy extends Strategy {
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
	private static int DESIRED_DISTANCE = 300;
	
	// END Regelparameter
	
	/**
	 * The motor speed
	 */
	private static int MAX_SPEED = 1000;
	private static int MOVE_ROTATION = 500;
	
	// TODO SB also for drive right
	/**
	 * Rotation speed
	 */
	private static int ROTATION_SPEED = 200;
	private static int ROTATION_DIRECTION = -1000;
	
	private boolean hasMoved;
	

	@Override
	protected void doInit() {
		startTachoCount = Motor.A.getTachoCount();
		endTachoCount = startTachoCount + DESIRED_DISTANCE;
		hasMoved = false;
	}
	
	private boolean shouldMove() {
		return !hasMoved && Motor.A.getTachoCount() < endTachoCount;
	}

	@Override
	protected void doRun() {
		if(shouldMove()) {
			System.out.println("move");
			ENGINE.move(MAX_SPEED, MOVE_ROTATION);
		} else {
			System.out.println("turn");
			hasMoved = true;
			ENGINE.move(ROTATION_SPEED, ROTATION_DIRECTION);
		}
	}
	
}
