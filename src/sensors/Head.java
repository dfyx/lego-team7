package sensors;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Delay;

public class Head implements Sensor<Integer> {

	private static final NXTRegulatedMotor MOTOR = Motor.C;
	private static final NXTMotor RAW_MOTOR = new NXTMotor(MotorPort.C);

	int positionX; // -1000: 90° left, 0: centered, 1000: 90° right
	int positionY; // -1000: bottom, 0: top
	int currentHorizontalBorderPos;
	boolean currentHorizontalBorderIsLeft;
	int HORIZONTAL_ANGLE;
	int VERTICAL_ANGLE;
	int topLeftPos;
	int topCentered;
	int topRightPos;
	int bottomLeftPos;
	int bottomCentered;
	int bottomRightPos;

	private SweepThread sweepThread = new SweepThread();

	public Head() {
		calibrate();
		sweepThread.start();
	}

	@Override
	public void poll() {
		// TODO Might be used to allow synchronisation with main loop
	}

	@Override
	public Integer getValue() {
		// TODO maybe just return the last sensor reading
		return null;
	}
	
	public void checkMoveTarget(int moveTarget) {
		if(moveTarget>bottomRightPos || moveTarget<topLeftPos)
			throw new IllegalStateException("Move out of range");
	}

	public void moveTo(int x, int y) {
		//Move down on the right side
		if(x==positionX && positionX==1000 && y<=positionY) {
			int moveTarget = topRightPos - y*(bottomRightPos-topRightPos)/1000;
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = false;
			positionY=y;
			checkMoveTarget(moveTarget);
			MOTOR.rotateTo(moveTarget);
		}
		//Move up on the left side
		else if (x==positionX && positionX==0 && y>=positionY) {
			int moveTarget = topLeftPos - y*(bottomLeftPos-topLeftPos)/1000;
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = true;
			positionY=y;
			checkMoveTarget(moveTarget);
			MOTOR.rotateTo(moveTarget);
		}
		//Move horizontally
		else if (y==positionY) {
			if(currentHorizontalBorderIsLeft) {
				int moveTarget = currentHorizontalBorderPos + HORIZONTAL_ANGLE/2 + x*HORIZONTAL_ANGLE/2000;
				positionX=x;
				checkMoveTarget(moveTarget);
				MOTOR.rotateTo(moveTarget);
			} else {
				int moveTarget = currentHorizontalBorderPos - HORIZONTAL_ANGLE/2 + x*HORIZONTAL_ANGLE/2000;
				positionX=x;
				checkMoveTarget(moveTarget);
				MOTOR.rotateTo(moveTarget);
			}
		}
		//Move arbitrarily down
		else if(y<=positionY) {
			moveTo(1000,positionY);
			moveTo(1000,y);
			moveTo(x,y);
		}
		//Move arbitratily up
		else { //y>positionY
			moveTo(0,positionY);
			moveTo(0,y);
			moveTo(x,y);
		}
	}

	public void calibrate() {
		final int MIN_MOVEMENT = 12;
		int power = 25;

		boolean round = false;

		MOTOR.suspendRegulation();
		for (int i = 0; i < 2; ++i) {
			RAW_MOTOR.setPower(power);
			// MOTOR.forward();
			Delay.msDelay(100);
			int lastPosition = RAW_MOTOR.getTachoCount();
			int position = RAW_MOTOR.getTachoCount();
			int min = Integer.MAX_VALUE;
			int max = Integer.MIN_VALUE;
			do {
				int diff = Math.abs(lastPosition - position);
				// System.out.println("moving: " + diff);
				if (diff < min)
					min = diff;
				if (diff > max)
					max = diff;
				Delay.msDelay(100);
				lastPosition = position;
				position = RAW_MOTOR.getTachoCount();
				if (Button.ENTER.isDown()) {
					System.out.println("Pos: " + position);
				}
			} while (Math.abs(position - lastPosition) >= MIN_MOVEMENT);
			power = -power;

			if (!round) {
				bottomRightPos = position;
				round = true;
			} else {
				topLeftPos = position;
				break;
			}

		}
		MOTOR.stop();

		bottomRightPos -= 20;
		topLeftPos += 20;
		int distance = bottomRightPos - topLeftPos;
		HORIZONTAL_ANGLE = distance / 3;
		VERTICAL_ANGLE = distance - HORIZONTAL_ANGLE;
		topCentered = topLeftPos + HORIZONTAL_ANGLE / 2;
		topRightPos = topLeftPos + HORIZONTAL_ANGLE;
		bottomLeftPos = bottomRightPos - HORIZONTAL_ANGLE;
		bottomCentered = bottomRightPos - HORIZONTAL_ANGLE / 2;
		positionX = -1;
		currentHorizontalBorderPos = topLeftPos;
		currentHorizontalBorderIsLeft = true;
		
		moveTo(0,0);
		Button.waitForAnyPress();
		
		while(true) {
			moveTo(-1000,0);
			Button.waitForAnyPress();
			moveTo(-1000,-1000);
			Button.waitForAnyPress();
			moveTo(0,-1000);
			Button.waitForAnyPress();
			moveTo(0,0);
			Button.waitForAnyPress();
			moveTo(1000,0);
			Button.waitForAnyPress();
			moveTo(1000,-1000);
			Button.waitForAnyPress();
		}

		/*System.out.println("BR: " + bottomRightPos);
		System.out.println("BL: " + bottomLeftPos);
		System.out.println("TR: " + topRightPos);
		System.out.println("TL: " + topLeftPos);

		int HORIZONTAL_SPEED = 1000;
		int VERTICAL_SPEED = 500;

		int basePosition = MOTOR.getTachoCount();
		MOTOR.setSpeed(HORIZONTAL_SPEED);
		MOTOR.rotateTo(topRightPos);
		MOTOR.rotateTo(topLeftPos);
		MOTOR.rotateTo(topRightPos);
		MOTOR.setSpeed(VERTICAL_SPEED);
		MOTOR.rotateTo(bottomRightPos);
		MOTOR.setSpeed(HORIZONTAL_SPEED);
		MOTOR.rotateTo(bottomLeftPos);
		MOTOR.rotateTo(bottomRightPos);
		MOTOR.rotateTo(bottomLeftPos);
		MOTOR.setSpeed(VERTICAL_SPEED);
		MOTOR.rotateTo(topLeftPos);
		MOTOR.setSpeed(HORIZONTAL_SPEED);
		MOTOR.rotateTo((topLeftPos + topRightPos) / 2);
		MOTOR.stop();*/
	}

	class SweepThread extends Thread {
		@Override
		public void run() {

		}
	}
}
