package strategies.wall_follower;

import static robot.Platform.ENGINE;
import lejos.nxt.Motor;
import strategies.Strategy;

/**
 * Used to adjust the robot at the end of a wall
 * 
 * @author sebastian
 * 
 */
public class AvoidCrashStrategy extends Strategy {
	/**
	 * Maximum rotation time in ms
	 */
	private static final long MAX_ROTATION_TIME = 2000;
	private static long endRotationTime;
	
	static final int CRASH_DISTANCE = 13;

	/**
	 * Rotation speed
	 */
	private static int ROTATION_SPEED = 500;
	private static int ROTATION_DIRECTION = 1000;

	private boolean hasTurned;

	// TODO SB make this work with sensor on both sides
	@Override
	protected void doInit() {
		endRotationTime = System.currentTimeMillis() + MAX_ROTATION_TIME;
		WallFollowerController.faceForward();
	}

	@Override
	protected void doRun() {
		ENGINE.move(ROTATION_SPEED, ROTATION_DIRECTION);
	}

}
