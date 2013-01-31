package sensors;

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
	private static final double VERTICAL_FACTOR_DOWN = 0.7;
	private static final double VERTICAL_FACTOR_UP = 0.79;

	private int positionX; // -1000: full left, 0: centered, 1000: full right
	private int positionY; // -1000: bottom, 0: top
	private int currentHorizontalBorderPos; //The motor position origin for x movement. This is the motor position of either the left or the right border of the current horizontal movement. 
	private boolean currentHorizontalBorderIsLeft; //True, iff currentHorizontalBorderPos stores the left border of the current horizontal movement.
	private int horizontalAngleRight; //The horizontal movement range (in motor pos), when currentHorizontalBorderIsLeft==false
	private int verticalAngleDown; //The vertical movement range when moving down
	private int horizontalAngleLeft; //The horizontal movement range (in motor pos), when currentHorizontalBorderIsLeft==true
	private int verticalAngleUp; //The vertical movement range when moving up
	//Some motor positions
	private int topLeftPos;
	private int topRightPos;
	private int bottomLeftPos;
	private int bottomRightPos;

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
	
	/**
	 * Return the x coordinate of the current head position
	 * 
	 * -1000<=result<=1000 whereas -1000: leftmost, 0: centered, 1000: rightmost
	 */
	public int getPositionX() {
		return positionX;
	}
	
	/**
	 * Return the y coordinate of the current head position
	 * 
	 * -1000<=result<=0 whereas -1000: bottom, 0: top
	 */
	public int getPositionY() {
		return positionY;
	}
	
	/**
	 * Return the current value of the distance sensor
	 */
	public int getDistance() {
		return SENSOR.getDistance();
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
	 * @param x The x coordinate. Range as given in Javadoc for getPositionX()
	 * @param y The y coordinate. Range as given in Javadoc for getPositionY()
	 */
	public void moveTo(int x, int y) {
		System.out.println("Move from "+positionX+"/"+positionY+" to "+x+"/"+y);
		System.out.flush();
		
		//Move down on the right side
		if(x==positionX && positionX==1000 && y<=positionY) {
			int moveTarget = topRightPos - y*(bottomRightPos-topRightPos)/1000;
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = false;
			positionY=y;
			System.out.println("-> Move down (on right side) to "+moveTarget);
			System.out.flush();
			doRotateTo(moveTarget);
		}
		//Move up on the left side
		else if (x==positionX && positionX==-1000 && y>=positionY) {
			int moveTarget = topLeftPos - y*(bottomLeftPos-topLeftPos)/1000;
			currentHorizontalBorderPos = moveTarget;
			currentHorizontalBorderIsLeft = true;
			positionY=y;
			System.out.println("-> Move up (on left side) to "+moveTarget);
			System.out.flush();
			doRotateTo(moveTarget);
		}
		//Move horizontally
		else if (y==positionY) {
			if(currentHorizontalBorderIsLeft) {
				int moveTarget = currentHorizontalBorderPos + horizontalAngleLeft/2 + x*horizontalAngleLeft/2000;
				positionX=x;
				System.out.println("-> Move horizontally (from left) to "+moveTarget);
				System.out.flush();
				doRotateTo(moveTarget);
			} else {
				int moveTarget = currentHorizontalBorderPos - horizontalAngleRight/2 + x*horizontalAngleRight/2000;
				positionX=x;
				System.out.println("-> Move horizontally (from right) to "+moveTarget);
				System.out.flush();
				doRotateTo(moveTarget);
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
	
	/**
	 * The minimal movement per cycle when calibrating.
	 * If this movement isn't done, the algorithm stops
	 * and says it's at the corner and can't move anymore.
	 */
	private final static int MIN_MOVEMENT = 12;
	
	/**
	 * The voltage to use for calibration
	 */
	private final int CALIBRATION_POWER = 40;
	
	//Perform a motor rotation to the given motor coordinates
	private void doRotateTo(int motorPos) {
		if(motorPos>bottomRightPos || motorPos<topLeftPos)
			throw new IllegalStateException("Move out of range: "+motorPos);
		System.out.println("--rotate to "+motorPos);
		System.out.flush();
		MOTOR.rotateTo(motorPos);
	}
	
	//Calibrate one of the corners using the given power
	//The sign of the power determines fixes the direction.
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
	
	/**
	 * Recalibrate the bottom right corner
	 */
	public void calibrateBottomRight() {
		bottomRightPos = doCalibrateDirection(CALIBRATION_POWER) - 100;
		System.out.println("bottomRight: "+bottomRightPos);
		System.out.flush();
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
		//System.out.println("Recalibrate");
		topLeftPos = doCalibrateDirection(-CALIBRATION_POWER) + 140;
		System.out.println("topLeft: "+topLeftPos);
		System.out.flush();
		recalcPositions();
		
		doRotateTo(topLeftPos);
		positionX = -1000;
		positionY = 0;
		currentHorizontalBorderPos = topLeftPos;
		currentHorizontalBorderIsLeft = true;
		//System.out.println("Recalibrate finished");
	}
	
	//Recalculate the positions after recalibrating one of the corners
	private void recalcPositions() {
		int distance = bottomRightPos - topLeftPos;
		verticalAngleDown = (int)(VERTICAL_FACTOR_DOWN*distance);
		horizontalAngleRight = distance-verticalAngleDown;
		verticalAngleUp = (int)(VERTICAL_FACTOR_UP*distance);
		horizontalAngleLeft = distance-verticalAngleUp;		
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
	 * Number of values that are stored in one horizontal sweep.
	 * The complete horizontal range is divided into VALUECOUNT
	 * measurement points.
	 */
	private static final int VALUECOUNT=10;
	
	//The upper horizontal line of measurement values
	private int distancesUp[] = new int[VALUECOUNT];
	//The lower horizontal line of measurement values
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
	 * @param xPos The index of the value to return. Must be >=0 and <getHorizontalValueCount().
	 */
	public int getUpperValue(int xPos) {
		return distancesUp[xPos];
	}

	/**
	 * Return one of the measured values in the lower sweep line.
	 * 
	 * @param xPos The index of the value to return. Must be >=0 and <getHorizontalValueCount().
	 */
	public int getLowerValue(int xPos) {
		return distancesDown[xPos];
	}

	//The thread used for sweeping
	private class SweepThread extends Thread {
		
		private boolean isRunning=true;
		
		public void pause() {
			isRunning=false;
		}
		
		public void restart() {
			isRunning=true;
		}
		
		private void doPause() {
			while(!isRunning)
				Delay.msDelay(100);
		}
		
		@Override
		public void run() {
			while(true) {
				//Scan upper line
				for(int i=0;i<VALUECOUNT;++i) {
					doPause();
					moveTo(-1000+i*2000/(VALUECOUNT-1),0);
					distancesUp[i]=SENSOR.getDistance();
				}
				for(int i=0;i<VALUECOUNT;++i) {
					doPause();
					moveTo(1000-i*2000/(VALUECOUNT-1),-1000);
					distancesDown[VALUECOUNT-i-1]=SENSOR.getDistance();
				}
			}
		}
	}
}
