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
	
	private static final int SPEED = 1000;	

	// How long to wait at least between recalibration attempts (in ms)
	private static final int RECALIBRATION_MIN_INTERVAL = 10000;

	/**
	 * The minimal movement per cycle when calibrating. If this movement isn't
	 * done, the algorithm stops and says it's at the corner and can't move
	 * anymore.
	 */
	private final static int MIN_MOVEMENT = 5;

	// How much of space to spare on the left and on the right (in motor units)
	private final static int CALIBRATION_OFFSET = 10;

	/**
	 * The voltage to use for calibration
	 */
	private final int CALIBRATION_POWER = 40;

	// When was the last head calibration?
	private int lastCalibrationLeft = 0;
	private int lastCalibrationRight = 0;

	// Range for moving
	private int mostLeftPos;
	private int mostRightPos;

	// Current state
	private boolean isMoving = false;
	private boolean isCalibrating = false;
	private int target;
	private boolean reMove = false; // Abort current movement and continue with next move command
	private boolean stopMoving = false; //Abort current movement
	
	public HeadMotor() {
		start();
	}
	
	private void doRotateTo(int target, boolean async) {
		int motorPos=(mostLeftPos+mostRightPos)/2+target*(mostRightPos-mostLeftPos)/2000;
		MOTOR.stop();
		MOTOR.setSpeed(SPEED);
		if (motorPos > mostRightPos || motorPos < mostLeftPos)
			throw new IllegalArgumentException("Move out of range: " + motorPos);
		MOTOR.rotateTo(motorPos,async);
	}

	public synchronized boolean isMoving() {
		return isMoving;
	}
	
	public synchronized void stopMoving() {
		stopMoving=true;
	}

	public synchronized boolean isCalibrating() {
		return isCalibrating;
	}

	public int getPosition() {
		return -1000+2000 * (MOTOR.getTachoCount() - mostLeftPos)
				/ (mostRightPos - mostLeftPos);
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

		Delay.msDelay(100);
		int lastPosition = RAW_MOTOR.getTachoCount();
		int position = RAW_MOTOR.getTachoCount();
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		do {
			int diff = Math.abs(lastPosition - position);
			if (diff < min)
				min = diff;
			if (diff > max)
				max = diff;
			Delay.msDelay(200);
			lastPosition = position;
			position = RAW_MOTOR.getTachoCount();
			System.out.println("Diff: "+Math.abs(position-lastPosition));
		} while (Math.abs(position - lastPosition) >= MIN_MOVEMENT);

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
		System.out.println("Most right: "+mostRightPos);
		System.out.flush();
		MOTOR.rotateTo(mostRightPos);
		System.out.println("Right calibrated");
		System.out.flush();
	}

	/**
	 * Recalibrate the top left corner
	 */
	private void calibrateLeft() {
		mostLeftPos = doCalibrateDirection(-CALIBRATION_POWER)
				+ CALIBRATION_OFFSET;
		lastCalibrationLeft = Utils.getSystemTime();
		System.out.println("Most left: "+mostLeftPos);
		System.out.flush();
		MOTOR.rotateTo(mostLeftPos);
		System.out.println("Left calibrated");
		System.out.flush();
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
			calibrate();
			while (true) {
				// State is NOT_MOVING
				while (!isMoving) {
					Delay.msDelay(10);
				}
				// State just switched to MOVING
				doRotateTo(target, true);
				while (MOTOR.isMoving() && !reMove && !stopMoving) {
					Delay.msDelay(10);
				}
				if(stopMoving) {
					MOTOR.stop();
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
						synchronized(this) {
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
						synchronized(this) {
							isCalibrating = false;
						}
					} else {
						synchronized(this) {
							isMoving=false;
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
