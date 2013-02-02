package sensors;

import lejos.nxt.UltrasonicSensor;
import robot.Platform;

public class Head implements Sensor<Integer> {

	private static final UltrasonicSensor SENSOR = new UltrasonicSensor(
			Platform.ULTRASONIC_PORT);
	
	// Last polled sensor values
	private int polledDistance;
	private int polledPosition;
	private int[] polledSweepValues;

	//private MotorThread motorThread = new MotorThread();
	private HeadMotor headMotor = new HeadMotor();
	private SweepThread sweepThread = new SweepThread(headMotor,SENSOR);

	@Override
	public void poll() {
		polledPosition = headMotor.getPosition();
		polledDistance = SENSOR.getDistance();
		polledSweepValues = sweepThread.getValues();
	}

	@Override
	public Integer getValue() {
		return polledDistance;
	}

	/**
	 * Returns true, iff the sensor head is currently moving
	 */
	public boolean isMoving() {
		return headMotor.isMoving();
	}
	
	public boolean isCalibrating() {
		return headMotor.isCalibrating();
	}

	/**
	 * Return the current head position. You must call
	 * poll() before calling this function.
	 * 
	 * -1000<=result<=1000 whereas -1000: leftmost, 0: centered, 1000: rightmost
	 */
	public int getPosition() {
		return polledPosition;
	}

	/**
	 * Stop sweeping. Call this function, if you want to position the head
	 * manually using moveTo(x,y)
	 */
	public void stopSweeping() {
		sweepThread.stopSweeping();
	}

	/**
	 * Start sweeping
	 * 
	 * @param from
	 *            The leftmost x position to sweep. See getPositionX() for
	 *            range.
	 * @param to
	 *            The rightmost x position to sweep. See getPositionX() for
	 *            range.
	 * @param valuecount
	 *            The number of values to scan, distributed over the sweeping
	 *            area
	 */
	public void startSweeping(int from, int to, int valuecount) {
		sweepThread.startSweeping(from,to,valuecount);
	}

	/**
	 * Move the head manually to a given position
	 * 
	 * @param position
	 *            The new position. Range as given in Javadoc for getPosition()
	 * @param async
	 *            If true, the call immediately returns while the head is still
	 *            moving.
	 */
	public void moveTo(int position, boolean async) {
		headMotor.moveTo(position, async);
	}

	/**
	 * Return the measured sweep values Lower indices in the array are values
	 * more to the left
	 */
	public int[] getSweepValues() {
		return polledSweepValues;
	}
}
