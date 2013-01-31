package strategies.wall_follower;

import static robot.Platform.ENGINE;
import lejos.nxt.Motor;
import strategies.Strategy;

/**
 * Used to adjust the robot at the end of a wall
 * @author sebastian
 *
 */
public class EdgeStrategy extends Strategy {
	/**
	 * count of the motor, when the robot looses the wall
	 */
	int startTachoCount;
	/**
	 * count of the motor, when we want to start the curve
	 */
	int endTachoCount;
	
	/**
	 * In tacho count
	 */
	private static int DESIRED_DISTANCE = 1000;
	
	/**
	 * The motor speed
	 */
	private static int MAX_SPEED = 1000;
	
	

	@Override
	protected void doInit() {
		startTachoCount = Motor.A.getTachoCount();
		endTachoCount = startTachoCount + DESIRED_DISTANCE;
	}

	@Override
	protected void doRun() {
		System.out.println("Tacho: "+Motor.A.getTachoCount());
		if(Motor.A.getTachoCount() < endTachoCount)
			ENGINE.move(MAX_SPEED);
		else
			ENGINE.stop();
	}
	
}
