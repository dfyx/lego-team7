package sensors;

import lejos.util.Delay;

public class SweepThread extends Thread {
	
	private HeadMotor motor;
	private UltrasonicSensor ultrasonicSensor;
	private LightSensor lightSensor;
	
	private SyncArray ultrasonicValues = new SyncArray();
	private SyncArray lightValues = new SyncArray();
	private int sweepFrom;
	private int sweepTo;
	private int lightValueCount;
	private int ultrasonicValueCount;
	private int speed=1000;

	private boolean isRunning = false;
	private boolean terminate = false;
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
			int from = 0, to = 0;
			while (true) {
				while (!isRunning)
					Delay.msDelay(100);
				if(terminate)
					break;
				if (restart) {
					from = sweepFrom;
					to = sweepTo;
					// Move synchronously to the first point (necessary for
					// the following loop)
					motor.moveTo(from, false, speed);

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
				}
				if(lightMeasureThread.isRunning() || ultrasonicMeasureThread.isRunning())
					throw new IllegalStateException("Measurement still running");
				/*lightMeasureThread.stopMeasuring();
				ultrasonicMeasureThread.stopMeasuring();
				while(lightMeasureThread.isRunning() || ultrasonicMeasureThread.isRunning())
					Delay.msDelay(10);*/
				if(terminate)
					break;
				if (restart)
					continue;
				
				motor.moveTo(from, true, speed);
				lightMeasureThread.startMeasuring(lightValues.size()-1, 0, -1, to, from, motor.getPosition(), lightValues, lightSensor);
				ultrasonicMeasureThread.startMeasuring(ultrasonicValues.size()-1, 0, -1, to, from, motor.getPosition(), ultrasonicValues, ultrasonicSensor);
				while(motor.isMoving() && !restart && !terminate) {
					int motorPos = motor.getPosition();
					lightMeasureThread.setPosition(motorPos);
					ultrasonicMeasureThread.setPosition(motorPos);
				}
				if(lightMeasureThread.isRunning() || ultrasonicMeasureThread.isRunning())
					throw new IllegalStateException("Measurement still running");
				/*lightMeasureThread.stopMeasuring();
				ultrasonicMeasureThread.stopMeasuring();
				while(lightMeasureThread.isRunning() || ultrasonicMeasureThread.isRunning())
					Delay.msDelay(10);*/
				if(terminate)
					break;
			}
		} finally {
			motor.stopMoving();
			Delay.msDelay(1000);
		}
	}
}
