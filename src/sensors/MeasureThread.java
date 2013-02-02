package sensors;

import lejos.util.Delay;

public class MeasureThread extends Thread {

	private boolean isRunning = false;
	private boolean shouldStop = false;
	
	private int startIndex;
	private int targetIndex;
	private int indexInc;
	private int moveFrom;
	private int moveTo;
	private int currentPos;
	private SyncArray values;
	private Sensor sensor;
	
	public boolean isRunning() {
		return isRunning;
	}

	public void startMeasuring(int startIndex, int targetIndex, int indexInc, int moveFrom, int moveTo, int currentPos, SyncArray values, Sensor sensor) {
		this.startIndex = startIndex;
		this.targetIndex = targetIndex;
		this.indexInc = indexInc;
		this.moveFrom = moveFrom;
		this.moveTo = moveTo;
		this.currentPos = currentPos;
		this.values = values;
		this.sensor = sensor;
		isRunning = true;
	}

	public void stopMeasuring() {
		shouldStop=true;
	}
	
	public void setPosition(int pos) {
		currentPos = pos;
	}

	public MeasureThread() {
		start();
	}

	@Override
	public void run() {
		while (true) {
			while (!isRunning) {
				Delay.msDelay(10);
			}
			while (!shouldStop) {
				int currentIndex = startIndex;
				while (currentIndex != targetIndex) {
					if(shouldStop)
						break;
					if (currentPos >= moveFrom + (moveTo - moveFrom) * currentIndex
							/ (values.size() - 1)) {
						values.write(currentIndex, sensor.getValue());
						currentIndex += indexInc;
					}
					Delay.msDelay(1);
				}
			}
			shouldStop=false;
			isRunning=false;
		}
	}

}
