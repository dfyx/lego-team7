import lejos.nxt.LCD;
import lejos.util.Delay;


public class SensorArm extends SensorArmBase {
	
	private int leftAngle = -45;
	private int rightAngle = 45;
	private int leftCheckAngle = -5;
	private int rightCheckAngle = 5;
	private int centralAngle = (rightAngle + leftAngle) / 2;
	private int steps = 5;

	private int maxLight;
	private int minLight;

	public void calibrateLine() {
		// Switch on light
		getLightSensor().setFloodlight(true);

		// Measure light while moving arm
		minLight = Integer.MAX_VALUE;
		maxLight = Integer.MIN_VALUE;
		getMotor().rotateTo(leftAngle);
		double anglePerStep = ((double) (rightAngle - leftAngle)) / steps;
		for (int i = 0; i < steps; ++i) {
			getMotor().rotateTo(leftAngle + (int) (i * anglePerStep));
			int value = getLightSensor().getLightValue();
			if (value < minLight)
				minLight = value;
			if (value > maxLight)
				maxLight = value;
		}
	}
	
	public void calibrateLine2(Engine engine) {
		//Get base value
		getLightSensor().setFloodlight(false);
		Delay.msDelay(100);
		final int baseLight = getLightSensor().getLightValue();
		System.out.println("Base value: "+baseLight);
		
		// Switch on light
		getLightSensor().setFloodlight(true);

		// Measure light while moving arm
		minLight = Integer.MAX_VALUE;
		maxLight = Integer.MIN_VALUE;
		engine.move(1000,true);
		while(engine.isMoving()) {
			int value = getLightSensor().getLightValue();
			if (value < minLight)
				minLight = value;
			if (value > maxLight)
				maxLight = value;
			LCD.drawString("min: "+minLight+"   ",2,0);
			LCD.drawString("max: "+maxLight+"   ",3,0);
		}
	}
	
	public void findLine() {
		getMotor().rotateTo(leftCheckAngle);
		int leftLightValue = getLightSensor().getLightValue();
		getMotor().rotateTo(rightCheckAngle);
		int rightLightValue = getLightSensor().getLightValue();
	}

	
	public void moveCentral() {
		getMotor().rotateTo(centralAngle);
	}
	
	public int isOnLine() {
		//moveCentral();
		getLightSensor().setFloodlight(true);
		int measured = getLightSensor().getLightValue();
		int normalized = 100 * (measured - minLight) / (maxLight - minLight);
		return normalized;
	}
}
