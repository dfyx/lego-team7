package sensors;

import java.util.Arrays;

import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;
import robot.Platform;
import utils.Utils;

public class Head implements Sensor<Integer> {

	private static final NXTRegulatedMotor MOTOR = Motor.C;
	private static final NXTMotor RAW_MOTOR = new NXTMotor(MotorPort.C);
	private static final UltrasonicSensor SENSOR = new UltrasonicSensor(
			Platform.ULTRASONIC_PORT);

	// Factor of horizontal movement of the complete movement range
	private static final double VERTICAL_FACTOR_DOWN = 0.7;
	private static final double VERTICAL_FACTOR_UP = 0.79;

	private static final int HORIZONTAL_SPEED = 1000;
	private static final int VERTICAL_SPEED = 1000;
	
	public static final int NOT_SCANNED_YET = Integer.MAX_VALUE;

	private int positionX; // -1000: full left, 0: centered, 1000: full right
	private int positionY; // -1000: bottom, 0: top
	private int currentHorizontalBorderPos; // The motor position origin for x
											// movement. This is the motor
											// position of either the left or
											// the right border of the current
											// horizontal movement.
	private boolean currentHorizontalBorderIsLeft; // True, iff
													// currentHorizontalBorderPos
													// stores the left border of
													// the current horizontal
													// movement.
	private int horizontalAngleRight; // The horizontal movement range (in motor
										// pos), when
										// currentHorizontalBorderIsLeft==false
	private int verticalAngleDown; // The vertical movement range when moving
									// down
	private int horizontalAngleLeft; // The horizontal movement range (in motor
										// pos), when
										// currentHorizontalBorderIsLeft==true
	private int verticalAngleUp; // The vertical movement range when moving up
	// Some motor positions
	private int topLeftPos;
	private int topRightPos;
	private int bottomLeftPos;
	private int bottomRightPos;

	// Last polled sensor values
	private int polledDistance;
	private int polledPositionX;
	private int polledPositionY;

	private class Monitor {
	}

	private Monitor sweepValuesMonitor = new Monitor();

	private MotorThread motorThread = new MotorThread();
	private SweepThread sweepThread = new SweepThread();

	public Head() {
		calibrate();
		motorThread.start();
		sweepThread.start();
	}

	@Override
	public void poll() {
		if (motorThread.isMoving()) {
			polledPositionX = calcXPos();
			polledPositionY = calcYPos();
		} else {
			polledPositionX = positionX;
			polledPositionY = positionY;
			polledDistance = SENSOR.getDistance();
		}
	}

	public int calcXPos() {
		int motorPos = MOTOR.getTachoCount();
		int x;
		if (currentHorizontalBorderIsLeft) {
			x = -1000
					+ (int) (2000 * ((double) (motorPos - currentHorizontalBorderPos)) / horizontalAngleLeft);
		} else {
			x = 1000 - (int) (2000 * ((double) (currentHorizontalBorderPos - motorPos)) / horizontalAngleRight);
		}
		return Utils.clamp(x, -1000, 1000);
	}

	public int calcYPos() {
		int motorPos = MOTOR.getTachoCount();
		int x = calcXPos();
		int y;
		if (x == -1000) {
			y = (int) ((double) (-1000 * (motorPos - topLeftPos)) / verticalAngleUp);
			y = Utils.clamp(y, -1000, 0);
		} else if (x >= 1000) {
			y = (int) ((double) (-1000 * (verticalAngleDown - (bottomRightPos - motorPos))) / verticalAngleDown);
			y = Utils.clamp(y, -1000, 0);
		} else
			y = positionY;
		return y;
	}

	@Override
	public Integer getValue() {
		return polledDistance;
	}

	/**
	 * Returns true, iff the sensor head is currently moving
	 */
	public boolean isMoving() {
		return motorThread.isMoving();
	}

	/**
	 * Return the x coordinate of the current head position. You must call
	 * poll() before getting the position.
	 * 
	 * -1000<=result<=1000 whereas -1000: leftmost, 0: centered, 1000: rightmost
	 */
	public int getPositionX() {
		return polledPositionX;
	}

	/**
	 * Return the y coordinate of the current head position. You must call
	 * poll() before getting the position.
	 * 
	 * -1000<=result<=0 whereas -1000: bottom, 0: top
	 */
	public int getPositionY() {
		return polledPositionY;
	}

	/**
	 * Stop sweeping. Call this function, if you want to position the head
	 * manually using moveTo(x,y)
	 */
	public void stopSweeping() {
		sweepThread.pause();
	}

	private int[] sweepValues;
	boolean sweepAtTop;
	int sweepFromX;
	int sweepToX;
	int valueCount;

	/**
	 * Start sweeping
	 * 
	 * @param xFrom
	 *            The leftmost x position to sweep. See getPositionX() for
	 *            range.
	 * @param xTo
	 *            The rightmost x position to sweep. See getPositionX() for
	 *            range.
	 * @param top
	 *            True, if the y position for sweeping should be top. False, if
	 *            it should be bottom.
	 * @param valuecount
	 *            The number of values to scan, distributed over the sweeping
	 *            area
	 */
	public void startSweeping(int xFrom, int xTo, boolean top, int valuecount) {
		sweepAtTop = top;
		sweepFromX = xFrom;
		sweepToX = xTo;
		valueCount = valuecount;

		sweepThread.restart();
	}

	/**
	 * Move the head manually to a given position
	 * 
	 * @param x
	 *            The x coordinate. Range as given in Javadoc for getPositionX()
	 * @param y
	 *            The y coordinate. Range as given in Javadoc for getPositionY()
	 * @param async
	 *            If true, the call immediately returns while the head is still
	 *            moving.
	 */
	public void moveTo(int x, int y, boolean async) {
		if (async) {
			motorThread.moveTo(x, y);
			return;
		} else {
			while (motorThread.isMoving()) {
				Delay.msDelay(10);
			}
			moveSync(x, y);
		}
	}

	private int lastCalibrationTopLeft = 0;
	private int lastCalibrationBottomRight = 0;
	// How long to wait at least between recalibration attempts (in ms)
	private static final int RECALIBRATION_MIN_INTERVAL = 10000;

	/**
	 * Move the head manually to a given position. The method is synchronous and
	 * returns only after the movement is complete.
	 * 
	 * @param x
	 *            The x coordinate. Range as given in Javadoc for getPositionX()
	 * @param y
	 *            The y coordinate. Range as given in Javadoc for getPositionY()
	 */
	private synchronized void moveSync(int x, int y) {

		// Move down on the right side
		if (x == positionX && positionX == 1000 && y <= positionY) {
			int moveTarget = topRightPos - y * (bottomRightPos - topRightPos)
					/ 1000;
			MOTOR.stop();
			MOTOR.setSpeed(VERTICAL_SPEED);
			doRotateTo(moveTarget);
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = false;
			positionY = y;
		}
		// Move up on the left side
		else if (x == positionX && positionX == -1000 && y >= positionY) {
			int moveTarget = topLeftPos - y * (bottomLeftPos - topLeftPos)
					/ 1000;
			MOTOR.stop();
			MOTOR.setSpeed(VERTICAL_SPEED);
			doRotateTo(moveTarget);
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = true;
			positionY = y;
		}
		// Move horizontally
		else if (y == positionY) {
			if (currentHorizontalBorderIsLeft) {
				int moveTarget = currentHorizontalBorderPos
						+ horizontalAngleLeft / 2 + x * horizontalAngleLeft
						/ 2000;
				MOTOR.stop();
				MOTOR.setSpeed(HORIZONTAL_SPEED);
				doRotateTo(moveTarget);
				positionX = x;
			} else {
				int moveTarget = currentHorizontalBorderPos
						- horizontalAngleRight / 2 + x * horizontalAngleRight
						/ 2000;
				MOTOR.stop();
				MOTOR.setSpeed(HORIZONTAL_SPEED);
				doRotateTo(moveTarget);
				positionX = x;
			}
		}
		// Move arbitrarily down
		else if (y <= positionY) {
			moveSync(1000, positionY);
			if (y != positionY)
				moveSync(1000, y);
			if (x != 1000)
				moveSync(x, y);
		}
		// Move arbitratily up
		else { // y>positionY
			moveSync(-1000, positionY);
			if (y != positionY)
				moveSync(-1000, y);
			if (x != -1000)
				moveSync(x, y);
		}

		if (x == -1000
				&& y == 0
				&& lastCalibrationTopLeft + RECALIBRATION_MIN_INTERVAL < Utils
						.getSystemTime()) {
			calibrateTopLeft();
			lastCalibrationTopLeft = Utils.getSystemTime();
		} else if (x == 1000
				&& y == -1000
				&& lastCalibrationBottomRight + RECALIBRATION_MIN_INTERVAL < Utils
						.getSystemTime()) {
			calibrateBottomRight();
			lastCalibrationBottomRight = Utils.getSystemTime();
		}
		// Button.waitForAnyPress();
	}

	/**
	 * The minimal movement per cycle when calibrating. If this movement isn't
	 * done, the algorithm stops and says it's at the corner and can't move
	 * anymore.
	 */
	private final static int MIN_MOVEMENT = 12;

	/**
	 * The voltage to use for calibration
	 */
	private final int CALIBRATION_POWER = 40;

	// Perform a motor rotation to the given motor coordinates
	private void doRotateTo(int motorPos) {
		if (motorPos > bottomRightPos || motorPos < topLeftPos)
			throw new IllegalArgumentException("Move out of range: " + motorPos);
		MOTOR.rotateTo(motorPos);
	}

	private boolean calibrating;

	// Calibrate one of the corners using the given power
	// The sign of the power determines fixes the direction.
	private int doCalibrateDirection(int power) {
		calibrating = true;
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
			Delay.msDelay(100);
			lastPosition = position;
			position = RAW_MOTOR.getTachoCount();
		} while (Math.abs(position - lastPosition) >= MIN_MOVEMENT);

		MOTOR.stop();
		calibrating = false;
		return position;
	}

	/**
	 * Recalibrate the bottom right corner
	 */
	public void calibrateBottomRight() {
		bottomRightPos = doCalibrateDirection(CALIBRATION_POWER) - 100;
		recalcPositions();

		doRotateTo(bottomRightPos);
		positionX = 1000;
		positionY = -1000;
		currentHorizontalBorderPos = bottomRightPos;
		currentHorizontalBorderIsLeft = false;
	}

	/**
	 * Recalibrate the top left corner
	 */
	public void calibrateTopLeft() {
		topLeftPos = doCalibrateDirection(-CALIBRATION_POWER) + 140;
		recalcPositions();

		doRotateTo(topLeftPos);
		positionX = -1000;
		positionY = 0;
		currentHorizontalBorderPos = topLeftPos;
		currentHorizontalBorderIsLeft = true;
	}

	// Recalculate the positions after recalibrating one of the corners
	private void recalcPositions() {
		int distance = bottomRightPos - topLeftPos;
		verticalAngleDown = (int) (VERTICAL_FACTOR_DOWN * distance);
		horizontalAngleRight = distance - verticalAngleDown;
		verticalAngleUp = (int) (VERTICAL_FACTOR_UP * distance);
		horizontalAngleLeft = distance - verticalAngleUp;
		topRightPos = topLeftPos + horizontalAngleLeft;
		bottomLeftPos = bottomRightPos - horizontalAngleRight;
	}

	/**
	 * Recalibrate the complete head movement
	 */
	public void calibrate() {
		calibrateBottomRight();
		calibrateTopLeft();
	}

	/**
	 * Return the measured sweep values Lower indices in the array are values
	 * more to the left
	 */
	public int[] getSweepValues() {
		synchronized (sweepValuesMonitor) {
			return Arrays.copyOf(sweepValues, sweepValues.length);
		}
	}

	// This thread handles asynchronous motor movements
	private class MotorThread extends Thread {
		private boolean isMoving = false;
		private int targetX;
		private int targetY;

		public boolean isMoving() {
			return isMoving;
		}

		public void moveTo(int x, int y) {
			targetX = x;
			targetY = y;
			isMoving = true;
		}

		public void run() {
			try {
				while (true) {
					while (!isMoving || calibrating) {
						Delay.msDelay(10);
					}
					Head.this.moveSync(targetX, targetY); // Move synchronously
					isMoving = false;
				}
			} finally {
				MOTOR.stop();
			}
		}
	}

	// The thread used for sweeping
	private class SweepThread extends Thread {

		private boolean isRunning = false;
		private boolean restart;

		public void pause() {
			isRunning = false;
		}

		public void restart() {
			restart = true;
			isRunning = true;
		}

		private void doPause() {
			while (!isRunning)
				Delay.msDelay(100);
		}

		@Override
		public void run() {
			try {
				int y = 0, from = 0, to = 0;
				while (true) {
//System.out.println("1");
					while(isMoving())
						Delay.msDelay(1);
					doPause();
					//System.out.println("2");
					if (restart) {
						//System.out.println("3");
						if (sweepAtTop)
							y = 0;
						else
							y = -1000;
						from = sweepFromX;
						to = sweepToX;
						//System.out.println("4");

						// Init sweepValues
						synchronized (sweepValuesMonitor) {
							sweepValues = new int[valueCount];
							for (int i = 0; i < valueCount; ++i) {
								sweepValues[i] = NOT_SCANNED_YET;
							}
						}
						//System.out.println("5");
						restart = false;
					}
					// Scan left to right
					/*
					 * while(isMoving()) { Delay.msDelay(1); }
					 */
					moveTo(to, y, true);
					//System.out.println("6");
					int currentIndex = 0;
					while (isMoving()/* && currentIndex<sweepValues.length */) {
						//System.out.println("7");
						doPause();
						if (restart)
							break;
						int x = calcXPos();
						//System.out.println("8");
						if (x >= from + (to - from) * currentIndex
								/ (sweepValues.length - 1)) {
							sweepValues[currentIndex] = SENSOR.getDistance();
							++currentIndex;
						}
						Delay.msDelay(1);
					}
					//System.out.println("9");
					if (restart)
						continue;
					//System.out.println("10");
					if (currentIndex != sweepValues.length)
						throw new IllegalStateException(
								"sweepValues.length!=currentIndex");
					currentIndex -= 2;
					//System.out.println("11");
					/*
					 * while(isMoving()) { Delay.msDelay(1); }
					 */
					moveTo(from, y, true);
					//System.out.println("12");
					while (isMoving()/* && currentIndex>=0 */) {
						//System.out.println("13");
						doPause();
						//System.out.println("14");
						if (restart)
							break;
						//System.out.println("15");
						int x = calcXPos();
						//System.out.println("16");
						if (x <= from + (to - from) * currentIndex
								/ (sweepValues.length - 1)) {
							//System.out.println("17");
							sweepValues[currentIndex] = SENSOR.getDistance();
							--currentIndex;
						}
						//System.out.println("18");
						Delay.msDelay(1);
					}
					if (restart)
						continue;
					//System.out.println("19");
					if (currentIndex != -1)
						throw new IllegalStateException("-1!=currentIndex=="
								+ currentIndex);
				}
			} finally {
				System.out.println("Motor stopped");
				MOTOR.stop();
			}
		}
	}
}
