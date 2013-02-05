package sensors;

import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Delay;
import strategies.Action;
import utils.Queue;
import utils.Utils;

// This thread handles asynchronous motor movements
class HeadMotor implements Action {

	private enum State {
		INACTIVE, CALIBRATING_LEFT, CALIBRATING_RIGHT, MOVING, FLOATING
	};

	private State state;

	private static final NXTRegulatedMotor MOTOR = Motor.C;
	private static final NXTMotor RAW_MOTOR = new NXTMotor(MotorPort.C);

	// How long to wait at least between recalibration attempts (in ms)
	private static final int RECALIBRATION_MIN_INTERVAL = 10000;

	/**
	 * The minimal movement per cycle when calibrating. If this movement isn't
	 * done, the algorithm stops and says it's at the corner and can't move
	 * anymore.
	 */
	private final static int MIN_MOVEMENT = 1;

	// How much of space to spare on the left and on the right (in motor units)
	private final static int CALIBRATION_OFFSET = 10;

	/**
	 * The voltage to use for calibration
	 */
	private final int CALIBRATION_POWER = 50;
	private final int CALIBRATION_MEASUREDELAY = 200;

	// When was the last head calibration?
	private int lastCalibrationLeft = 0;
	private int lastCalibrationRight = 0;

	// Range for moving
	private int mostLeftPos;
	private int mostRightPos;

	private Queue<State> stateQueue;

	// Current state
	/**
	 * the tacho count, when collision detection is started
	 */
	private int collisionTachoCount;

	public void terminate() {
		// TODO
	}

	public HeadMotor() {
		stateQueue = new Queue<State>();
		stateQueue.push(State.CALIBRATING_RIGHT);
		enterState(State.CALIBRATING_LEFT);
	}

	private void enterState(State newState) {
		if (newState == null) {
			MOTOR.stop(true);
			state = State.INACTIVE;
			return;
		}

		switch (newState) {
		case CALIBRATING_LEFT:
			MOTOR.suspendRegulation();
			RAW_MOTOR.setPower(-CALIBRATION_POWER);
			lastMotorPos = RAW_MOTOR.getTachoCount();
			state = State.CALIBRATING_LEFT;
			calibratingLastTime = Utils.getSystemTime();
			break;
		case CALIBRATING_RIGHT:
			MOTOR.suspendRegulation();
			RAW_MOTOR.setPower(CALIBRATION_POWER);
			lastMotorPos = RAW_MOTOR.getTachoCount();
			state = State.CALIBRATING_RIGHT;
			calibratingLastTime = Utils.getSystemTime();
			break;
		case MOVING:
			doMove();
			break;
		case FLOATING:
		case INACTIVE:
			state = newState;
		}
	}

	private int calibratingLastTime;
	private int lastMotorPos;

	@Override
	public void run() {
		int currentTime = Utils.getSystemTime();
		switch (state) {
		case INACTIVE:
			break;
		case CALIBRATING_LEFT:
			if (currentTime >= calibratingLastTime + CALIBRATION_MEASUREDELAY) {
				int motorPos = RAW_MOTOR.getTachoCount();
				if (lastMotorPos - motorPos < MIN_MOVEMENT) {
					lastCalibrationLeft = currentTime;
					mostLeftPos = RAW_MOTOR.getTachoCount()
							+ CALIBRATION_OFFSET;
					enterState(stateQueue.pop());
				} else
					calibratingLastTime = currentTime;
				lastMotorPos = motorPos;
			}
			break;
		case CALIBRATING_RIGHT:
			if (currentTime >= calibratingLastTime + CALIBRATION_MEASUREDELAY) {
				int motorPos = RAW_MOTOR.getTachoCount();
				if (motorPos - lastMotorPos < MIN_MOVEMENT) {
					lastCalibrationRight = currentTime;
					mostRightPos = RAW_MOTOR.getTachoCount()
							- CALIBRATION_OFFSET;
					enterState(stateQueue.pop());
				} else
					calibratingLastTime = currentTime;
				lastMotorPos = motorPos;
			}
			break;
		case MOVING:
			if (!MOTOR.isMoving()) {
				if(getPosition()>900 && lastCalibrationRight+RECALIBRATION_MIN_INTERVAL<currentTime) {
					stateQueue.push(State.MOVING);
					enterState(State.CALIBRATING_RIGHT);
				} else if(getPosition()<-900  && lastCalibrationLeft+RECALIBRATION_MIN_INTERVAL<currentTime) {
					stateQueue.push(State.MOVING);
					enterState(State.CALIBRATING_LEFT);
				} else {
					enterState(State.INACTIVE);
				}
			}
			break;
		case FLOATING:
			break;
		}
	}

	public void detectCollisions(boolean detect) {
		collisionTachoCount = MOTOR.getTachoCount();
		if (detect) {
			MOTOR.flt(true);
			state = State.FLOATING;
		} else {
			MOTOR.stop(true);
			state = State.MOVING;
			// State change to INACTIVE done automatically when motor stops
		}
	}

	public boolean isColliding() {
		int currentTachoCount = MOTOR.getTachoCount();
		int diff = collisionTachoCount - currentTachoCount;
		if (diff < 0)
			diff *= -1;
		return diff > 3;
	}

	public boolean isMoving() {
		return state == State.MOVING || state == State.CALIBRATING_LEFT
				|| state == State.CALIBRATING_RIGHT;
	}

	public void stopMoving() {
		MOTOR.stop(true);
		// State change to inactive is done automatically when moving ends
	}

	public boolean isCalibrating() {
		return state == State.CALIBRATING_LEFT
				|| state == State.CALIBRATING_RIGHT;
	}

	public int getPosition() {
		return -1000 + 2000 * (MOTOR.getTachoCount() - mostLeftPos)
				/ (mostRightPos - mostLeftPos);
	}

	public void moveToSync(int position, int speed) {
		MOTOR.stop();
		while (isMoving()) {
			Delay.msDelay(10);
		}
		moveTo(position, speed);

		while (isMoving()) {
			Delay.msDelay(10);
		}
	}
	
	private int newPosition;
	private int newSpeed;

	public void moveTo(int position, int speed) {
		newPosition = position;
		newSpeed = speed;
		
		switch (state) {
		case INACTIVE:
		case MOVING:
		case FLOATING:
			enterState(State.MOVING);
			break;
		case CALIBRATING_LEFT:
		case CALIBRATING_RIGHT:
			stateQueue.push(State.MOVING);
		}
	}

	private void doMove() {
		int motorPos = (mostLeftPos + mostRightPos) / 2 + newPosition
				* (mostRightPos - mostLeftPos) / 2000;

		if (motorPos > mostRightPos || motorPos < mostLeftPos)
			throw new IllegalArgumentException("Move out of range: " + motorPos);
		MOTOR.setSpeed(newSpeed);
		MOTOR.rotateTo(motorPos, true);
		state = State.MOVING;
	}
}
