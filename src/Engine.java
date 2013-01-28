import lejos.nxt.NXTRegulatedMotor;

import lejos.nxt.Motor;


public class Engine {
	private NXTRegulatedMotor leftMotor = Motor.A;
	private NXTRegulatedMotor rightMotor = Motor.B;
	
	public void rotate(int degrees) {
		rotate(degrees,false);
	}
	
	public void rotate(int degrees, boolean immediateReturn) {
		leftMotor.rotate(-degrees,true);
		rightMotor.rotate(degrees,immediateReturn);
	}
	
	public void move(int distance) {
		move(distance,false);
	}
	
	public void move(int distance, boolean immediateReturn) {
		leftMotor.rotate(distance,true);
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
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		leftMotor.forward();
		rightMotor.backward();
	}
	
	public void startMoving(int speed) {
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
		leftMotor.forward();
		rightMotor.forward();
	}
	
	/*public void setSpeed(int speed) {
		leftMotor.setSpeed(speed);
		rightMotor.setSpeed(speed);
	}*/
}
