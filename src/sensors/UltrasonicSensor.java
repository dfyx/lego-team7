package sensors;

import lejos.nxt.SensorPort;

//Only used inside of this package
class UltrasonicSensor implements Sensor {
	
	private final lejos.nxt.UltrasonicSensor realSensor;
	
	private MeasureThread thread = new MeasureThread();
	
	public UltrasonicSensor(final SensorPort port) {
		realSensor = new lejos.nxt.UltrasonicSensor(port);
		thread.start();
	}
	
	private volatile int value;
	
	public synchronized int getValue() {
		return value;
	}
	
	public void terminate() {
		thread.interrupt();
	}
	
	private synchronized void setValue(int newValue) {
		value = newValue;
	}
	
	private class MeasureThread extends Thread {
		@Override
		public void run() {
			while(!interrupted()) {
				int distance = realSensor.getDistance();
				setValue(distance);
			}
		}
	}
}
