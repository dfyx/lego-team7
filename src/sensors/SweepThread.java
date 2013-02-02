package sensors;

import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;

public class SweepThread extends Thread {
	
	private HeadMotor motor;
	private UltrasonicSensor ultrasonicSensor;
	private LightSensor lightSensor;
	
	private SyncArray ultrasonicValues = new SyncArray();
	private SyncArray lightValues = new SyncArray();
	private int sweepFrom;
	private int sweepTo;
	private int valueCount;

	private boolean isRunning = false;
	private boolean restart;
	
	public SweepThread(HeadMotor motor, UltrasonicSensor uS, LightSensor lS) {
		this.motor=motor;
		ultrasonicSensor=uS;
		lightSensor=lS;
		start();
	}
	
	public int[] getUltrasonicValues() {
		return ultrasonicValues.getCopy();
	}
	
	public int[] getLightValues() {
		return lightValues.getCopy();
	}

	/**
	 * Start sweeping
	 * 
	 * @param from
	 *            The leftmost x position to sweep. See HeadMotor.getPosition() for
	 *            range.
	 * @param to
	 *            The rightmost x position to sweep. See HeadMotor.getPosition() for
	 *            range.
	 * @param valuecount
	 *            The number of values to scan, distributed over the sweeping
	 *            area
	 */
	public void startSweeping(int from, int to, int valuecount) {
		sweepFrom = from;
		sweepTo = to;
		valueCount = valuecount;

		restart=true;
		isRunning=true;
	}

	public void stopSweeping() {
		isRunning = false;
	}

	private void doPause() {
		while (!isRunning)
			Delay.msDelay(100);
	}

	@Override
	public void run() {
		try {
			int from = 0, to = 0;
			while (true) {
				doPause();
				if (restart) {
					from = sweepFrom;
					to = sweepTo;
					// Move synchronously to the first point (necessary for
					// the following loop)
					motor.moveTo(from, false);

					// Init sweepValues
					lightValues.init(valueCount);
					ultrasonicValues.init(valueCount);
					restart = false;
				}
				// Scan left to right
				motor.moveTo(to, true);
				int currentIndex = 0;
				while (currentIndex<lightValues.size()) {
					doPause();
					if (restart)
						break;
					int x = motor.getPosition();
					if (x >= from + (to - from) * currentIndex
							/ (lightValues.size() - 1)) {
						ultrasonicValues.write(currentIndex, ultrasonicSensor.getDistance());
						lightValues.write(currentIndex, lightSensor.getValue());
						++currentIndex;
					}
					Delay.msDelay(1);
				}
				if (restart)
					continue;
				if (currentIndex != lightValues.size())
					throw new IllegalStateException(
							"sweepValues.length!=currentIndex");
				currentIndex -= 2;
				motor.moveTo(from, true);
				while (currentIndex>0) {
					doPause();
					if (restart)
						break;
					int x = motor.getPosition();
					if (x <= from + (to - from) * currentIndex
							/ (lightValues.size() - 1)) {
						ultrasonicValues.write(currentIndex, ultrasonicSensor.getDistance());
						lightValues.write(currentIndex, lightSensor.getValue());
						--currentIndex;
					}
					Delay.msDelay(1);
				}
				if (restart)
					continue;
				if (currentIndex != 0)
					throw new IllegalStateException("0!=currentIndex=="
							+ currentIndex);
			}
		} finally {
			motor.stopMoving();
			Delay.msDelay(1000);
		}
	}
}
