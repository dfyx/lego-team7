package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import static robot.Platform.ULTRASONIC_PORT;
import lejos.nxt.UltrasonicSensor;
import utils.Utils;

// TODO SB should work for right and left looking sensor head
public class WallFollowerStrategy extends Strategy {
	// TODO SB calibrate?
	/**
	 * desired distance to wall (in mm)
	 */
	private int referenceValue = 150;

	private static final int MAX_SPEED = 500;
	private int speed = MAX_SPEED;

	private enum HeadOn {
		RIGHT_SIDE, LEFT_SIDE
	}

	private static HeadOn headOn;

	/**
	 * Turn on max speed outside of +- 5cm corridor 50*_20_ = 1000
	 */
	private static final int LINEAR_FACTOR = 10;

	private static final int LINEAR_FACTOR_MOVE_AWAY = 7;
	private static final int LINEAR_FACTOR_MOVE_TOWARDS = 5;

	UltrasonicSensor realSensor;

	/**
	 * distance to wall (in mm)
	 */
	private int actualValue;

	public WallFollowerStrategy() {

	}

	protected void doInit() {
		realSensor = new UltrasonicSensor(ULTRASONIC_PORT);
		realSensor.setMode(UltrasonicSensor.MODE_CONTINUOUS);
		headOn = HeadOn.LEFT_SIDE;
		
		HEAD.stopSweeping();
		if(headOn == HeadOn.LEFT_SIDE)
			HEAD.moveTo(-1000, 0, false);
		else
			HEAD.moveTo(1000, 0, false);
	}

	protected void doRun() {
	    // doInit is moving the sensor head nonblocking, skip control loop
	    // until the sensor head arrived at its final position 
	    if (HEAD.isMoving()) {
	        return;
	    }
	    
		// TODO SB set to real sensor data
		// read data and convert to mm
		actualValue = realSensor.getDistance() * 10;

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
		if (linearValue > 0)
			linearValue = diff * LINEAR_FACTOR_MOVE_TOWARDS;
		else
			linearValue = diff * LINEAR_FACTOR_MOVE_AWAY;

		return Utils.clamp(linearValue, -1000, 1000);
	}
}
