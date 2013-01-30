package strategies;

import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.RConsole;
import robot.Platform;
import utils.Utils;
import static robot.Platform.ENGINE;

// TODO SB should work for right and left looking sensor head
public class FollowWallStrategy extends Strategy {
	// TODO SB calibrate?
	/**
	 * desired distance to wall (in mm)
	 */
	private int referenceValue = 100;

	private int speed = 1000;

	private enum HeadOn {
		RIGHT_SIDE, LEFT_SIDE
	}

	private static HeadOn headOn;

	/**
	 * Turn on max speed outside of +- 5cm corridor 50*_20_ = 1000
	 */
	private static final int LINEAR_FACTOR = 20;
	
	UltrasonicSensor realSensor;

	/**
	 * distance to wall (in mm)
	 */
	private int actualValue;
	
	public FollowWallStrategy() {
		
	}

	protected void doInit() {
		realSensor = new lejos.nxt.UltrasonicSensor(Platform.ULTRASONIC_PORT);
		realSensor.setMode(UltrasonicSensor.MODE_CONTINUOUS);
		headOn = HeadOn.RIGHT_SIDE;
	}

	protected void doRun() {
		// TODO SB set to real sensor data
		// read data and convert to mm
		actualValue = realSensor.getDistance()*10;

		int direction = getMotorSpeed();

		if (headOn == HeadOn.LEFT_SIDE)
			direction = -direction;
		
		System.out.println("IST/SOLL: " + actualValue + " / " + referenceValue + " -> " + direction);
		
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
