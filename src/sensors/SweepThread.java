package sensors;

import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;

public class SweepThread extends Thread {
	
	private HeadMotor MOTOR;
	private UltrasonicSensor SENSOR;
	
	private SyncArray sweepValues = new SyncArray();
	private int sweepFrom;
	private int sweepTo;
	private int valueCount;

	private boolean isRunning = false;
	private boolean restart;
	
	public SweepThread(HeadMotor motor, UltrasonicSensor sensor) {
		MOTOR=motor;
		SENSOR=sensor;
		start();
	}
	
	public int[] getValues() {
		return sweepValues.getCopy();
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
					MOTOR.moveTo(from, false);

					// Init sweepValues
					sweepValues.init(valueCount);
					restart = false;
				}
				// Scan left to right
				MOTOR.moveTo(to, true);
				int currentIndex = 0;
				while (currentIndex<sweepValues.size()) {
					doPause();
					if (restart)
						break;
					int x = MOTOR.getPosition();
					if (x >= from + (to - from) * currentIndex
							/ (sweepValues.size() - 1)) {
						sweepValues.write(currentIndex, SENSOR.getDistance());
						++currentIndex;
					}
					Delay.msDelay(1);
				}
				if (restart)
					continue;
				if (currentIndex != sweepValues.size())
					throw new IllegalStateException(
							"sweepValues.length!=currentIndex");
				currentIndex -= 2;
				MOTOR.moveTo(from, true);
				while (currentIndex>0) {
					doPause();
					if (restart)
						break;
					int x = MOTOR.getPosition();
					if (x <= from + (to - from) * currentIndex
							/ (sweepValues.size() - 1)) {
						sweepValues.write(currentIndex, SENSOR.getDistance());
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
			MOTOR.stopMoving();
			Delay.msDelay(1000);
		}
	}
}
