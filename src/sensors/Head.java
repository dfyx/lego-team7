package sensors;

import lejos.nxt.LightSensor;
import lejos.nxt.SensorPort;

public class Head implements Sensor {
	private LightSensor lightSensor = new LightSensor(SensorPort.S1);

	
}
