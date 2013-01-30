package sensors;

import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;
import robot.Platform;

public class Head implements Sensor<Integer> {

	private static final NXTRegulatedMotor MOTOR = Motor.C;
	private static final NXTMotor RAW_MOTOR = new NXTMotor(MotorPort.C);
	private static final UltrasonicSensor SENSOR = new UltrasonicSensor(Platform.ULTRASONIC_PORT);
	
	//Factor of horizontal movement of the complete movement range
	private static final double VERTICAL_FACTOR_DOWN = 0.72;
	private static final double VERTICAL_FACTOR_UP = 0.58;

	int positionX; // -1000: full left, 0: centered, 1000: full right
	int positionY; // -1000: bottom, 0: top
	int currentHorizontalBorderPos;
	boolean currentHorizontalBorderIsLeft;
	int HORIZONTAL_ANGLE_RIGHT;
	int VERTICAL_ANGLE_DOWN;
	int HORIZONTAL_ANGLE_LEFT;
	int VERTICAL_ANGLE_UP;
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
			throw new IllegalStateException("Move out of range: "+moveTarget);
	}

	public void moveTo(int x, int y) {
		System.out.println("Move from "+positionX+"/"+positionY+" to "+x+"/"+y);
		
		//Move down on the right side
		if(x==positionX && positionX==1000 && y<=positionY) {
			int moveTarget = topRightPos - y*(bottomRightPos-topRightPos)/1000;
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = false;
			positionY=y;
			System.out.println("-> Move down (on right side) to "+moveTarget);
			checkMoveTarget(moveTarget);
			MOTOR.rotateTo(moveTarget);
		}
		//Move up on the left side
		else if (x==positionX && positionX==-1000 && y>=positionY) {
			int moveTarget = topLeftPos - y*(bottomLeftPos-topLeftPos)/1000;
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = true;
			positionY=y;
			System.out.println("-> Move up (on left side) to "+moveTarget);
			checkMoveTarget(moveTarget);
			MOTOR.rotateTo(moveTarget);
		}
		//Move horizontally
		else if (y==positionY) {
			if(currentHorizontalBorderIsLeft) {
				int moveTarget = currentHorizontalBorderPos + HORIZONTAL_ANGLE_RIGHT/2 + x*HORIZONTAL_ANGLE_RIGHT/2000;
				positionX=x;
				System.out.println("-> Move horizontally (from left) to "+moveTarget);
				checkMoveTarget(moveTarget);
				MOTOR.rotateTo(moveTarget);
			} else {
				int moveTarget = currentHorizontalBorderPos - HORIZONTAL_ANGLE_LEFT/2 + x*HORIZONTAL_ANGLE_LEFT/2000;
				positionX=x;
				System.out.println("-> Move horizontally (from right) to "+moveTarget);
				checkMoveTarget(moveTarget);
				MOTOR.rotateTo(moveTarget);
			}
		}
		//Move arbitrarily down
		else if(y<=positionY) {
			moveTo(1000,positionY);
			if(y!=positionY)
				moveTo(1000,y);
			if(x!=1000)
				moveTo(x,y);
		}
		//Move arbitratily up
		else { //y>positionY
			moveTo(-1000,positionY);
			if(y!=positionY)
				moveTo(-1000,y);
			if(x!=-1000)
				moveTo(x,y);
		}
		
		if(x==-1000 && y==0)
			calibrateTopLeft();
		else if(x==1000 && y==-1000)
			calibrateBottomRight();
		//Button.waitForAnyPress();
	}
	
	private final static int MIN_MOVEMENT = 12;
	private final int CALIBRATION_POWER = 35;
	
	private int doCalibrateDirection(int power) {
		MOTOR.suspendRegulation();
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
			//if (Button.ENTER.isDown()) {
			//	System.out.println("Pos: " + position);
			//}
			//System.out.println("Speed: "+(position-lastPosition));
		} while (Math.abs(position - lastPosition) >= MIN_MOVEMENT);

		MOTOR.stop();
		return position;
	}
	
	public void calibrateBottomRight() {
		bottomRightPos = doCalibrateDirection(CALIBRATION_POWER) - 75;
		recalcPositions();
		
		checkMoveTarget(bottomRightPos);
		MOTOR.rotateTo(bottomRightPos);
		positionX = 1000;
		positionY = -1000;
		currentHorizontalBorderPos = bottomRightPos;
		currentHorizontalBorderIsLeft = false;
	}
	
	public void calibrateTopLeft() {
		System.out.println("Recalibrate");
		topLeftPos = doCalibrateDirection(-CALIBRATION_POWER) + 75;
		recalcPositions();
		
		checkMoveTarget(topLeftPos);
		MOTOR.rotateTo(topLeftPos);
		positionX = -1000;
		positionY = 0;
		currentHorizontalBorderPos = topLeftPos;
		currentHorizontalBorderIsLeft = true;
		System.out.println("Recalibrate finished");
	}
	
	private void recalcPositions() {
		int distance = bottomRightPos - topLeftPos;
		VERTICAL_ANGLE_DOWN = (int)(VERTICAL_FACTOR_DOWN*distance);
		HORIZONTAL_ANGLE_RIGHT = distance-VERTICAL_ANGLE_DOWN;
		VERTICAL_ANGLE_UP = (int)(VERTICAL_FACTOR_UP*distance);
		HORIZONTAL_ANGLE_LEFT = distance-VERTICAL_ANGLE_UP;		
		topCentered = topLeftPos + HORIZONTAL_ANGLE_RIGHT / 2;
		topRightPos = topLeftPos + HORIZONTAL_ANGLE_RIGHT;
		bottomLeftPos = bottomRightPos - HORIZONTAL_ANGLE_LEFT;
		bottomCentered = bottomRightPos - HORIZONTAL_ANGLE_LEFT / 2;
	}

	public void calibrate() {
		
		calibrateBottomRight();
		calibrateTopLeft();
		
		System.out.println("topleft: "+topLeftPos);
		System.out.println("bottomright: "+bottomRightPos);
		
		System.out.println("TO TOP CENTER");
		moveTo(0,0);
		Button.waitForAnyPress();
		
		/*moveTo(-1000,0);
		moveTo(1000,0);
		moveTo(-1000,0);
		moveTo(1000,0);
		moveTo(-1000,0);
		moveTo(1000,0);
		moveTo(-1000,-1000);
		moveTo(1000,-1000);
		moveTo(-1000,-1000);
		moveTo(1000,-1000);
		moveTo(-1000,-1000);
		moveTo(1000,-1000);
		
		while(true) {
			System.out.println("TO TOP LEFT");
			moveTo(-1000,0);
			System.out.println("TO BOTTOM LEFT");
			moveTo(-1000,-1000);
			System.out.println("TO BOTTOM CENTER");
			moveTo(0,-1000);
			System.out.println("TO TOP CENTER");
			moveTo(0,0);
			System.out.println("TO TOP RIGHT");
			moveTo(1000,0);
			System.out.println("TO BOTTOM RIGHT");
			moveTo(1000,-1000);
		}*/

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
	
	static final int VALUECOUNT=10;
	
	private int distancesUp[] = new int[VALUECOUNT];
	private int distancesDown[] = new int[VALUECOUNT];

	class SweepThread extends Thread {
		@Override
		public void run() {
			while(true) {
				//Scan upper line
				for(int i=0;i<VALUECOUNT;++i) {
					moveTo(-1000+i*2000/VALUECOUNT,0);
					distancesUp[i]=SENSOR.getDistance();
				}
				for(int i=0;i<VALUECOUNT;++i) {
					moveTo(1000-i*2000/VALUECOUNT,-1000);
					distancesDown[VALUECOUNT-i-1]=SENSOR.getDistance();
				}
			}
		}
	}
}
