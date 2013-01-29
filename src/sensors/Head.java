package sensors;

import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class Head implements Sensor {
	private LightSensor lightSensor = new LightSensor(SensorPort.S1);
	
	private int maxLight;
	private int minLight;

	/**
	 * Returns the light value, normalized between 0 and 1000.
	 */
	public int getLight() {
		int measured = lightSensor.getLightValue();
		int normalized = 1000 * (measured - minLight) / (maxLight - minLight);
		return normalized;
	}
	
	public void setFloodlight(boolean value) {
		lightSensor.setFloodlight(value);
	}
	
	/**
	 * Calibrate the light sensor
	 * 
	 * @param minValue The value mapped to 0
	 * @param maxValue The value mapped to 1000
	 */
	public void calibrateLight(int minValue, int maxValue) {
		minLight = minValue;
		maxLight = maxValue;
	}
}
