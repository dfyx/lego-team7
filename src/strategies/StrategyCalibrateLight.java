package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;
import lejos.nxt.LCD;

public class StrategyCalibrateLight extends Strategy {

	final double LINEFOUND_FACTOR = 5;
	final int LINEFOUND_COUNT = 20;
	final int MOVE_MAX_SPEED = 600;

	private int cycle = 0;
	
	private int sum;
	private int count;
	private int baseLight;
	private int minLight;
	private int maxLight;
	private int lineValueCount;

	protected void doInit() {
		// Switch off calibration
		LIGHT_SENSOR.resetCalibration();
		LIGHT_SENSOR.setFloodlight(false);
		cycle = 0;
	}

	protected void doRun() {
		++cycle;
		
		if(cycle<10) {
		} else if(cycle==10) {
			ENGINE.move(100);
			sum=0;
			count=0;
		} else if(cycle<=110) {
			sum+=LIGHT_SENSOR.getRawValue();
			++count;
		} else if(cycle==120) {
			baseLight = sum/count;
			LIGHT_SENSOR.setFloodlight(true);
		} else if(cycle<130) {
		} else if(cycle==130) {
			minLight = Integer.MAX_VALUE;
			maxLight = Integer.MIN_VALUE;
			lineValueCount = 0;
			ENGINE.move(MOVE_MAX_SPEED);
		} else if (lineValueCount < LINEFOUND_COUNT) {
			int value = LIGHT_SENSOR.getRawValue();
			if (value < minLight)
				minLight = value;
			if (value > maxLight)
				maxLight = value;
			final int normalizedMin = minLight - baseLight;
			final int normalizedMax = maxLight - baseLight;
			final int normalizedValue = value - baseLight;
			if (LINEFOUND_FACTOR * normalizedValue < normalizedMax
					|| LINEFOUND_FACTOR * normalizedMin < normalizedValue)
				++lineValueCount;
			else if (lineValueCount > 1)
				lineValueCount -= 2;
			double speedFactorMin = 1 - ((double) normalizedValue)
					/ (5 * normalizedMin);
			double speedFactorMax = 1 - ((double) normalizedMax / (5 * normalizedValue));
			double speedFactor = Math.min(speedFactorMin, speedFactorMax);
			ENGINE.move((int) (speedFactor * MOVE_MAX_SPEED));
			LCD.drawString("min: " + (minLight - baseLight) + "   ", 0, 2);
			LCD.drawString("max: " + (maxLight - baseLight) + "   ", 0, 3);
			LCD.drawString("value: " + (value - baseLight) + "   ", 0, 4);
		} else {
			ENGINE.stop();
			// Set calibration
			LIGHT_SENSOR.calibrate(minLight, maxLight);
			setFinished();
		}
	}
}
