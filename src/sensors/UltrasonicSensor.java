package sensors;

import lejos.nxt.SensorPort;

//Only used inside of this package
class UltrasonicSensor implements Sensor {
	
	private final lejos.nxt.UltrasonicSensor realSensor; 
	
	public UltrasonicSensor(final SensorPort port) {
		realSensor = new lejos.nxt.UltrasonicSensor(port);
	}
	
	public int getValue() {
		return realSensor.getDistance();
	}
}
