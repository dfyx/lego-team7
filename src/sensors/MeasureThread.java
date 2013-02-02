package sensors;

import lejos.util.Delay;

public class MeasureThread extends Thread {

	private boolean isRunning = false;
	private boolean shouldStop = false;

	private int targetIndex;
	private int currentIndex;
	private int indexInc;
	private int moveFrom;
	private int moveTo;
	private int currentPos;
	private SyncArray values;
	private Sensor sensor;

	public boolean isRunning() {
		return isRunning;
	}

	public void startMeasuring(int startIndex, int targetIndex, int indexInc,
			int moveFrom, int moveTo, int currentPos, SyncArray values,
			Sensor sensor) {
		this.targetIndex = targetIndex;
		this.currentIndex = startIndex;
		this.indexInc = indexInc;
		this.moveFrom = moveFrom;
		this.moveTo = moveTo;
		this.currentPos = currentPos;
		this.values = values;
		this.sensor = sensor;
		isRunning = true;
	}

	public void stopMeasuring() {
		shouldStop = true;
	}

	public void setPosition(int pos) {
		currentPos = pos;
	}

	public MeasureThread() {
	}

	int i = 0;

	public void measureSync() {
		// System.out.println("current:"+currentPos+", moving from "+moveFrom+" to "+moveTo+", currentIndex="+currentIndex+", nextPos: "+(moveFrom
		// + (moveTo - moveFrom) * currentIndex / (values.size() -
		// 1))+"        ");
		if (isRunning && !interrupted())
			if (currentPos >= moveFrom + (moveTo - moveFrom) * currentIndex
					/ (values.size() - 1)) {
				values.write(currentIndex, sensor.getValue());
				currentIndex += indexInc;
				if (i == 0) {
					System.out.println("i=0, index="+currentIndex+", target="+targetIndex);
					System.out.flush();
				}
				// System.out.println("Measuring "+currentIndex+", i: "+i);
				i = 0;
			} else
				++i;
		if (currentIndex == targetIndex)
			isRunning = false;
	}

	@Override
	public void run() {
		while (true) {
			while (!isRunning) {
				Delay.msDelay(10);
			}
			while (!shouldStop && currentIndex != targetIndex) {
				if (shouldStop)
					break;
				measureSync();
				Delay.msDelay(1);
			}
			shouldStop = false;
			isRunning = false;
		}
	}

}
