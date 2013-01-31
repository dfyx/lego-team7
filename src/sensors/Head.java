package sensors;

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

	//final Lock lock = new ReentrantLock();
	private MotorThread motorThread = new MotorThread();
	private SweepThread sweepThread = new SweepThread();

	public Head() {
		calibrate();
		motorThread.start();
		sweepThread.start();
	}

	@Override
	public void poll() {
		if(motorThread.isMoving()) {
			int motorPos = MOTOR.getTachoCount();
			if(currentHorizontalBorderIsLeft) {
				polledPositionX=-1000+(int)(2000*((double)(motorPos-currentHorizontalBorderPos))/horizontalAngleLeft);
			} else {
				polledPositionX=1000-(int)(2000*((double)(currentHorizontalBorderPos-motorPos))/horizontalAngleRight);
			}
			if(polledPositionX<=-1000) {
				polledPositionX=-1000;
				polledPositionY=(int)((double)(-1000*(motorPos-topLeftPos))/verticalAngleUp);
				polledPositionY=Utils.clamp(polledPositionY, -1000, 0);
			} else if(polledPositionX>=1000) {
				polledPositionX=1000;
				polledPositionY=(int)((double)(-1000*(verticalAngleDown-(bottomRightPos-motorPos)))/verticalAngleDown);
				polledPositionY=Utils.clamp(polledPositionY, -1000, 0);
			} else
				polledPositionY = positionY;
			polledDistance = SENSOR.getDistance();
		} else {
			polledPositionX = positionX;
			polledPositionY = positionY;
			polledDistance = SENSOR.getDistance();
		}
		//lock.unlock();
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
	public void pauseSweeping() {
		sweepThread.pause();
	}

	/**
	 * Restart sweeping after it was stopped with pauseSweeping().
	 */
	public void continueSweeping() {
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
	 * 			  If true, the call immediately returns while the head is still moving.
	 */
	public void moveTo(int x, int y, boolean async) {
		if(async) {
			motorThread.moveTo(x,y);
			return;
		} else {
			while(motorThread.isMoving()) {
				Delay.msDelay(10);
			}
			moveSync(x,y);
		}
	}

	/**
	 * Move the head manually to a given position.
	 * The method is synchronous and returns only after the movement is complete.
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

		if (x == -1000 && y == 0) {
			calibrateTopLeft();
		}
		else if (x == 1000 && y == -1000) {
			calibrateBottomRight();
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
	 * Number of values that are stored in one horizontal sweep. The complete
	 * horizontal range is divided into VALUECOUNT measurement points.
	 */
	private static final int VALUECOUNT = 10;

	// The upper horizontal line of measurement values
	private int distancesUp[] = new int[VALUECOUNT];
	// The lower horizontal line of measurement values
	private int distancesDown[] = new int[VALUECOUNT];

	/**
	 * Return, how many values are measured in one horizontal line
	 */
	public int getHorizontalValueCount() {
		return VALUECOUNT;
	}

	/**
	 * Return one of the measured values in the upper sweep line.
	 * 
	 * @param xPos
	 *            The index of the value to return. Must be >=0 and
	 *            <getHorizontalValueCount().
	 */
	public int getUpperValue(int xPos) {
		return distancesUp[xPos];
	}

	/**
	 * Return one of the measured values in the lower sweep line.
	 * 
	 * @param xPos
	 *            The index of the value to return. Must be >=0 and
	 *            <getHorizontalValueCount().
	 */
	public int getLowerValue(int xPos) {
		return distancesDown[xPos];
	}
	
	//This thread handles asynchronous motor movements
	private class MotorThread extends Thread {
		private boolean isMoving = false;
		private int targetX;
		private int targetY;
		
		public boolean isMoving() {
			return isMoving;
		}
		
		public void moveTo(int x, int y) {
			targetX=x;
			targetY=y;
			isMoving=true;
		}
		
		public void run() {
			while(true) {
				while(!isMoving || calibrating) {
					Delay.msDelay(10);
				}
				Head.this.moveSync(targetX,targetY); //Move synchronously
				isMoving=false;
			}
		}
	}

	// The thread used for sweeping
	private class SweepThread extends Thread {

		private boolean isRunning = true;

		public void pause() {
			isRunning = false;
		}

		public void restart() {
			isRunning = true;
		}

		private void doPause() {
			while (!isRunning)
				Delay.msDelay(100);
		}

		@Override
		public void run() {
			while (true) {
				// Scan upper line
				for (int i = 0; i < VALUECOUNT; ++i) {
					doPause();
					moveTo(-1000 + i * 2000 / (VALUECOUNT - 1), 0, false);
					distancesUp[i] = SENSOR.getDistance();
				}
				for (int i = 0; i < VALUECOUNT; ++i) {
					doPause();
					moveTo(1000 - i * 2000 / (VALUECOUNT - 1), -1000, false);
					distancesDown[VALUECOUNT - i - 1] = SENSOR.getDistance();
				}
			}
		}
	}
}
