package sensors;

import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Delay;
import utils.Utils;

// This thread handles asynchronous motor movements
class HeadMotor extends Thread {
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

	// When was the last head calibration?
	private int lastCalibrationLeft = 0;
	private int lastCalibrationRight = 0;

	// Range for moving
	private int mostLeftPos;
	private int mostRightPos;

	// Current state
	private volatile boolean fixedPowerMoving = false;
	private volatile boolean isMoving = false;
	private volatile boolean isCalibrating = false;
	private volatile int target;
	private volatile boolean reMove = false; // Abort current movement and
												// continue with next move
												// command
	private volatile boolean stopMoving = false; // Abort current movement
	private volatile int speed = 1000;

	private boolean terminate = false;

	public void terminate() {
		terminate = true;
	}

	public HeadMotor() {
		start();
	}
	
	public void setFloating(boolean floating) {
		if(floating)
			MOTOR.flt(true);
		else
			MOTOR.stop(true);
	}

	private void doRotateTo(int target, boolean async) {
		int motorPos = (mostLeftPos + mostRightPos) / 2 + target
				* (mostRightPos - mostLeftPos) / 2000;
		fixedPowerMoving = false;
		MOTOR.stop();
		MOTOR.setSpeed(speed);
		if (motorPos > mostRightPos || motorPos < mostLeftPos)
			throw new IllegalArgumentException("Move out of range: " + motorPos);
		MOTOR.rotateTo(motorPos, async);
	}

	private boolean isStalled = false;
	
	public void moveWithFixedPower(int power) {
		isStalled=false;
		fixedPowerMoving = true;
		MOTOR.suspendRegulation();
		RAW_MOTOR.setPower(power);
	}

	public boolean isStalled() {
		return isStalled;
	}

	public synchronized boolean isMoving() {
		return isMoving || fixedPowerMoving;
	}

	public synchronized void stopMoving() {
		stopMoving = true;
	}

	public synchronized boolean isCalibrating() {
		return isCalibrating;
	}

	public int getPosition() {
		return -1000 + 2000 * (MOTOR.getTachoCount() - mostLeftPos)
				/ (mostRightPos - mostLeftPos);
	}

	public void moveTo(int position, boolean async, int speed) {
		this.speed = speed;
		moveTo(position, async);
	}

	public void moveTo(int position, boolean async) {
		moveTo(position);
		if (!async) {
			while (isMoving) {
				Delay.msDelay(10);
			}
		}
	}

	public synchronized void moveTo(int position) {
		target = position;
		if (isMoving)
			reMove = true;
		isMoving = true;
	}

	// Calibrate one of the corners using the given power
	// The sign of the power determines fixes the direction.
	private int doCalibrateDirection(int power) {
		MOTOR.suspendRegulation();
		RAW_MOTOR.setPower(power);

		int lastPosition = RAW_MOTOR.getTachoCount();
		Delay.msDelay(200);
		int position = RAW_MOTOR.getTachoCount();
		while (Math.abs(position - lastPosition) >= MIN_MOVEMENT) {
			Delay.msDelay(200);
			lastPosition = position;
			position = RAW_MOTOR.getTachoCount();
		}
		;

		MOTOR.stop();
		return position;
	}

	/**
	 * Recalibrate the bottom right corner
	 */
	private void calibrateRight() {
		mostRightPos = doCalibrateDirection(CALIBRATION_POWER)
				- CALIBRATION_OFFSET;
		lastCalibrationRight = Utils.getSystemTime();
		MOTOR.rotateTo(mostRightPos);
	}

	/**
	 * Recalibrate the top left corner
	 */
	private void calibrateLeft() {
		mostLeftPos = doCalibrateDirection(-CALIBRATION_POWER)
				+ CALIBRATION_OFFSET;
		lastCalibrationLeft = Utils.getSystemTime();
		MOTOR.rotateTo(mostLeftPos);
	}

	/**
	 * Recalibrate the complete head movement
	 */
	public void calibrate() {
		isCalibrating = true;
		calibrateLeft();
		calibrateRight();
		isCalibrating = false;
	}

	public void run() {
		try {
			// Delay necessary, because motor commands are ignored when in debug
			// mode and issued too early
			Delay.msDelay(500);
			calibrate();
			while (!interrupted()) {
				int position = RAW_MOTOR.getTachoCount();
				int lastPosition = position;
				// State is NOT_MOVING
				while (!isMoving && !terminate) {
					while(!isMoving && !terminate && !fixedPowerMoving)
						Delay.msDelay(10);
					isStalled =  Math.abs(position - lastPosition) < MIN_MOVEMENT;
					lastPosition=position;
					position=RAW_MOTOR.getTachoCount();
					Delay.msDelay(10);
					if(stopMoving) {
						break;
					}
				}
				if (terminate)
					break;
				// State just switched to MOVING
				doRotateTo(target, true);
				while (MOTOR.isMoving() && !reMove && !stopMoving && !terminate && !fixedPowerMoving) {
					Delay.msDelay(10);
				}
				if (stopMoving) {
					MOTOR.stop();
				}
				if (terminate) {
					break;
				}
				// If state switched to reMove, remain MOVING state.
				// Otherwise switch back to NOT_MOVING
				if (!reMove) {
					// Recalibrate if possible
					if (target == -1000
							&& lastCalibrationLeft + RECALIBRATION_MIN_INTERVAL <= Utils
									.getSystemTime()) {
						synchronized (this) {
							isCalibrating = true;
							isMoving = false;
						}
						calibrateLeft();
						synchronized (this) {
							isCalibrating = false;
						}
					} else if (target == 1000
							&& lastCalibrationRight
									+ RECALIBRATION_MIN_INTERVAL <= Utils
										.getSystemTime()) {
						synchronized (this) {
							isCalibrating = true;
							isMoving = false;
						}
						calibrateRight();
						synchronized (this) {
							isCalibrating = false;
						}
					} else {
						synchronized (this) {
							isMoving = false;
						}
					}
				}
				reMove = false;
			}
		} finally {
			MOTOR.stop();
			Delay.msDelay(1000);
		}
	}
}
