package strategies.wall_follower.without_sweeping;

import strategies.Strategy;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import static robot.Platform.ENGINE;

public class NoSweepingWallFollowerStrategy extends Strategy {
	private static final int LAST_VALUES_COUNT = 10;
	private static int[] lastValuesLeft = new int[LAST_VALUES_COUNT];
	private static int[] lastValuesRight = new int[LAST_VALUES_COUNT];

	private enum State {
		START, START_MOTOR, BEGIN_DRIVING, DRIVING, WALL_FOUND, STOPPING
	}

	private static State currentState;

	private static final NXTRegulatedMotor LEFT_MOTOR = Motor.A;
	private static final NXTRegulatedMotor RIGHT_MOTOR = Motor.B;

	private static int oldTachoCountLeft = 0;
	private static int oldTachoCountRight = 0;
	
	private static int averageLeftSpeed() {
		int average = 0;
		for(int i : lastValuesLeft)
			average += i;
		return average/LAST_VALUES_COUNT;
	}
	
	private static int averageRightSpeed() {
		int average = 0;
		for(int i : lastValuesRight)
			average += i;
		return average/LAST_VALUES_COUNT;
	}

	private int leftTachoDiff;
	private int rightTachoDiff;

	@Override
	protected void doInit() {
		currentState = State.START;
		for (int i = 0; i < LAST_VALUES_COUNT; ++i) {
			lastValuesLeft[i] = 100;
			lastValuesRight[i] = 100;
		}
	}

	private State checkState() {
		State newState = currentState;
		switch (currentState) {
		case START:
			newState = State.START_MOTOR;
			break;
		case START_MOTOR:
			newState = State.BEGIN_DRIVING;
			break;
		case BEGIN_DRIVING:
			/*if ((LEFT_MOTOR.getTachoCount() - oldSpeedLeft) > 6
					&& (RIGHT_MOTOR.getTachoCount() - oldSpeedRight) > 6)*/
			newState = State.DRIVING;
			break;
		case DRIVING:
			if (leftTachoDiff > (averageLeftSpeed()*125)/100
					|| rightTachoDiff > (averageRightSpeed()*125)/100) {
				System.out.println("Wall found: " + averageLeftSpeed() + " / " + averageRightSpeed());
				newState = State.WALL_FOUND;
			}
			break;
		case WALL_FOUND:
			newState = State.STOPPING;
			break;
		case STOPPING:
			break;
		}
		System.out.println("State: " + newState);
		return newState;
	}

	@Override
	protected void doRun() {
		int newTachoCountLeft = LEFT_MOTOR.getTachoCount();
		int newTachoCountRight = RIGHT_MOTOR.getTachoCount();
		currentState = checkState();
		leftTachoDiff = (newTachoCountLeft - oldTachoCountLeft);
		rightTachoDiff = (newTachoCountRight - oldTachoCountRight);
		
		switch (currentState) {
		case START:
			break;
		case START_MOTOR:
			ENGINE.move(1000);
			break;
		case BEGIN_DRIVING:
			System.out.println("Starting: "
					+ leftTachoDiff + " , "
					+ rightTachoDiff);
			break;
		case DRIVING:
			System.out.println("Moving  : "
					+ leftTachoDiff + " , "
					+ rightTachoDiff);
			break;
		case WALL_FOUND:
			System.out.println("Stopping: "
					+ leftTachoDiff + " , "
					+ rightTachoDiff);
			ENGINE.stop();
			break;
		case STOPPING:
			break;
		}
		// print curve for start driving
		
		for (int i = 0; i < LAST_VALUES_COUNT-1; ++i) {
			lastValuesLeft[i] = lastValuesLeft[i+1];
			lastValuesRight[i] = lastValuesRight[i+1];
			System.out.print(lastValuesLeft[i]+", ");
		}
		System.out.println();
		lastValuesLeft[LAST_VALUES_COUNT-1] = leftTachoDiff;
		lastValuesRight[LAST_VALUES_COUNT-1] = rightTachoDiff;
		// for starting motor
		// average would be to small otherwise
		if(lastValuesLeft[LAST_VALUES_COUNT-1] < 4)
			lastValuesLeft[LAST_VALUES_COUNT-1] = 100;
		if(lastValuesRight[LAST_VALUES_COUNT-1] < 4)
			lastValuesRight[LAST_VALUES_COUNT-1] = 100;

		oldTachoCountLeft = newTachoCountLeft;
		oldTachoCountRight = newTachoCountRight;
	}
}
