import lejos.nxt.Button;
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
		final double LINEFOUND_FACTOR=5;
		final int LINEFOUND_COUNT=20;
		final int MOVE_MAX_SPEED = 600;
		
		//Get base value
		getLightSensor().setFloodlight(false);
		Delay.msDelay(100);
		engine.move(50,true);
		int sum=0;
		int count=0;
		while(engine.isMoving()) {
			sum += getLightSensor().getLightValue();
			++count;
		}
		final int baseLight = sum/count;
		LCD.drawString("base: "+count+"    ",0,0);
		
		// Switch on light
		getLightSensor().setFloodlight(true);
		Delay.msDelay(200);

		// Measure light while moving arm
		minLight = Integer.MAX_VALUE;
		maxLight = Integer.MIN_VALUE;
		engine.startMoving(MOVE_MAX_SPEED);
		int lineValueCount=0;
		while(lineValueCount<LINEFOUND_COUNT){
			//System.out.println(""+lineValueCount+"/"+LINEFOUND_COUNT);
			int value = getLightSensor().getLightValue();
			if (value < minLight)
				minLight = value;
			if (value > maxLight)
				maxLight = value;
			final int normalizedMin = minLight-baseLight;
			final int normalizedMax = maxLight-baseLight;
			final int normalizedValue = value-baseLight;
			if(LINEFOUND_FACTOR*normalizedValue<normalizedMax
					|| LINEFOUND_FACTOR*normalizedMin<normalizedValue)
				++lineValueCount;
			else if(lineValueCount>1)
				lineValueCount-=2;
			double speedFactorMin = 1-((double)normalizedValue)/(5*normalizedMin);
			double speedFactorMax = 1-((double)normalizedMax/(5*normalizedValue));
			double speedFactor = Math.min(speedFactorMin, speedFactorMax);
			engine.startMoving((int)(speedFactor*MOVE_MAX_SPEED));
			LCD.drawString("min: "+(minLight-baseLight)+"   ",0,2);
			LCD.drawString("max: "+(maxLight-baseLight)+"   ",0,3);
			LCD.drawString("value: "+(value-baseLight)+"   ",0,4);
		}
		engine.stop();
	}
	
	public void findLine() {
		getMotor().rotateTo(leftCheckAngle);
		int leftLightValue = getLightSensor().getLightValue();
		getMotor().rotateTo(rightCheckAngle);
		int rightLightValue = getLightSensor().getLightValue();
	}

	
	public void moveCentral() {
		//getMotor().rotateTo(centralAngle);
	}
	
	public int isOnLine() {
		//moveCentral();
		getLightSensor().setFloodlight(true);
		int measured = getLightSensor().getLightValue();
		int normalized = 100 * (measured - minLight) / (maxLight - minLight);
		return normalized;
	}
}
