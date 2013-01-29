import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class Engine {
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;

	public void rotate(int degrees) {
		rotate(degrees, false);
	}

	public void rotate(int degrees, boolean immediateReturn) {
		leftMotor.rotate(-degrees, true);
		rightMotor.rotate(degrees, immediateReturn);
	}

	public void move(int distance) {
		move(distance, false);
	}

	public void move(int distance, boolean immediateReturn) {
		leftMotor.rotate(distance, true);
		rightMotor.rotate(distance, immediateReturn);
	}

	public void stop() {
		leftMotor.stop();
		rightMotor.stop();
	}

	public boolean isMoving() {
		return leftMotor.isMoving();
	}

	public void startRotation(int speed) {
		if (speed == 0) {
			leftMotor.stop();
			rightMotor.stop();
		} else if (speed > 0) {
			leftMotor.setSpeed(speed);
			rightMotor.setSpeed(speed);
			leftMotor.backward();
			rightMotor.forward();
		} else {
			leftMotor.setSpeed(-speed);
			rightMotor.setSpeed(-speed);
			leftMotor.forward();
			rightMotor.backward();
		}
	}

	public void startMoving(int speed) {
		if (speed == 0) {
			leftMotor.stop();
			rightMotor.stop();
		} else if (speed > 0) {
			leftMotor.setSpeed(speed);
			rightMotor.setSpeed(speed);
			leftMotor.forward();
			rightMotor.forward();
		} else {
			leftMotor.setSpeed(speed);
			rightMotor.setSpeed(speed);
			leftMotor.backward();
			rightMotor.backward();
		}
	}

	//-1000 < speed < 1000
	//-1000 < direction < 1000
	public void startCurve(int speed, int direction) {
		if(1000<speed || -1000>speed || 1000<direction || -1000>direction)
			throw new IllegalStateException("Incorrect parameters");
		
		final int MAX=1000;

		int left = 500, right = 500;
		
		left-=direction;
		right+=direction;
		
		left *= 2;
		right *= 2;
		
		left=Math.min(MAX,Math.max(-MAX,left));
		right=Math.min(MAX,Math.max(-MAX,right));
		
		if(1000<right || -1000>right || 1000<left || -1000>left)
			throw new IllegalStateException("Incorrect intermediate values");
		
		left*=speed;
		right*=speed;
		left/=MAX;
		right/=MAX;
		
		if(1000<right || -1000>right || 1000<left || -1000>left)
			throw new IllegalStateException("Incorrect intermediate 2 values");
		
		if (left >= 0) {
			leftMotor.setSpeed(left);
			leftMotor.forward();
		} else {
			leftMotor.setSpeed(-left);
			leftMotor.backward();
		}
		if (right >= 0) {
			rightMotor.setSpeed(right);
			rightMotor.forward();
		} else {
			rightMotor.setSpeed(-right);
			rightMotor.backward();
		}
	}

	/*
	 * public void setSpeed(int speed) { leftMotor.setSpeed(speed);
	 * rightMotor.setSpeed(speed); }
	 */
}
