package sensors;

import java.security.InvalidParameterException;

import lejos.util.Delay;

public class SweepThread extends Thread {
	
	private HeadMotor motor;
	private UltrasonicSensor ultrasonicSensor;
	private LightSensor lightSensor;
	
	private SyncArray ultrasonicValues = new SyncArray();
	private SyncArray lightValues = new SyncArray();
	private volatile int sweepFrom;
	private volatile int sweepTo;
	private volatile int lightValueCount;
	private volatile int ultrasonicValueCount;
	private volatile int speed=1000;

	private volatile boolean isRunning = false;
	private volatile boolean terminate = false;
	private volatile boolean restart;
	
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
	
	public void setSpeed(int speed) {
		this.speed=speed;
	}
	
	public void terminate() {
		this.terminate=true;
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
	 * @param lightValuecount
	 *            The number of light values to scan, distributed over the sweeping
	 *            area
	 * @param ultrasonicValuecount
	 *            The number of distance values to scan, distributed over the sweeping
	 *            area
	 */
	public void startSweeping(int from, int to, int lightValuecount, int ultrasonicValuecount) {
		if(lightValueCount==1 || ultrasonicValueCount==1) {
			throw new IllegalArgumentException("We need at least two corner points for sweeping");
		}
		sweepFrom = from;
		sweepTo = to;
		lightValueCount = lightValuecount;
		ultrasonicValueCount = ultrasonicValuecount;

		restart=true;
		isRunning=true;
	}

	public void stopSweeping() {
		isRunning = false;
	}

	@Override
	public void run() {
		try {
			MeasureThread lightMeasureThread = new MeasureThread();
			MeasureThread ultrasonicMeasureThread = new MeasureThread();
			ultrasonicMeasureThread.start();
			int from = 0, to = 0;
			while (!interrupted()) {
				while (!isRunning)
					Delay.msDelay(100);
				if(terminate)
					break;
				if (restart) {
					from = sweepFrom;
					to = sweepTo;
					// Move synchronously to the first point (necessary for
					// the following loop)
					motor.moveTo(from, false, 1000);

					// Init sweepValues
					lightValues.init(lightValueCount);
					ultrasonicValues.init(ultrasonicValueCount);
					restart = false;
				}
				// Scan left to right
				motor.moveTo(to, true, speed);
				lightMeasureThread.startMeasuring(0,lightValues.size()-1,1,from,to,motor.getPosition(),lightValues,lightSensor);
				ultrasonicMeasureThread.startMeasuring(0,ultrasonicValues.size()-1,1,from,to,motor.getPosition(),ultrasonicValues,ultrasonicSensor);
				
				while(motor.isMoving() && !restart && !terminate) {
					int motorPos = motor.getPosition();
					lightMeasureThread.setPosition(motorPos);
					ultrasonicMeasureThread.setPosition(motorPos);
					lightMeasureThread.measureSync();
					Delay.msDelay(1);
				}
				if(terminate)
					break;
				if (restart)
					continue;
				//TODO Remove exception
				if(lightMeasureThread.isRunning() || ultrasonicMeasureThread.isRunning())
					throw new IllegalStateException("Measurement still running");
				
				motor.moveTo(from, true, speed);
				lightMeasureThread.startMeasuring(lightValues.size()-1, 0, -1, to, from, motor.getPosition(), lightValues, lightSensor);
				ultrasonicMeasureThread.startMeasuring(ultrasonicValues.size()-1, 0, -1, to, from, motor.getPosition(), ultrasonicValues, ultrasonicSensor);
				while(motor.isMoving() && !restart && !terminate) {
					int motorPos = motor.getPosition();
					lightMeasureThread.setPosition(motorPos);
					ultrasonicMeasureThread.setPosition(motorPos);
					lightMeasureThread.measureSync();
					Delay.msDelay(1);
				}
				if(terminate)
					break;
				//TODO Remove exception
				if(lightMeasureThread.isRunning() || ultrasonicMeasureThread.isRunning())
					throw new IllegalStateException("Measurement still running");
			}
		} finally {
			motor.stopMoving();
			Delay.msDelay(1000);
		}
	}
}
