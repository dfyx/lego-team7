package sensors;

import robot.Platform;

public class Head {

	private static final UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(
			Platform.ULTRASONIC_PORT);
	private static final LightSensor lightSensor = new LightSensor(
			Platform.LIGHT_PORT);

	// Last polled sensor values
	private int polledDistance;
	private int polledLight;
	private int polledPosition;
	private int[] polledUltrasonicValues;
	private int[] polledLightValues;

	// private MotorThread motorThread = new MotorThread();
	private HeadMotor headMotor = new HeadMotor();
	private SweepThread sweepThread = new SweepThread(headMotor,
			ultrasonicSensor, lightSensor);

	public void poll() {
		polledPosition = headMotor.getPosition();
		polledDistance = ultrasonicSensor.getValue();
		polledLight = lightSensor.getValue();
		polledUltrasonicValues = sweepThread.getUltrasonicValues();
		polledLightValues = sweepThread.getLightValues();
	}

	public int getDistance() {
		return polledDistance;
	}

	public int getLight() {
		return polledLight;
	}
	
	public void stopMoving() {
		headMotor.stopMoving();
	}
	
	public void terminate() {
		sweepThread.terminate();
		headMotor.terminate();
	}

	/**
	 * Switch floodlight on or off
	 */
	public void setFloodlight(boolean value) {
		lightSensor.setFloodlight(value);
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
	 * Return the current head position. You must call poll() before calling
	 * this function.
	 * 
	 * -1000<=result<=1000 whereas -1000: leftmost, 0: centered, 1000: rightmost
	 */
	public int getPosition() {
		return polledPosition;
	}

	/**
	 * Stop sweeping. Call this function, if you want to position the head
	 * manually using moveTo(pos)
	 */
	public void stopSweeping() {
		sweepThread.stopSweeping();
	}

	/**
	 * Start sweeping
	 * 
	 * @param from
	 *            The leftmost position to sweep. See getPosition() for range.
	 * @param to
	 *            The rightmost position to sweep. See getPosition() for range.
	 * @param lightValuecount
	 *            The number of light values to scan, distributed over the sweeping
	 *            area
	 * @param ultrasonicValuecount
	 *            The number of distance values to scan, distributed over the sweeping
	 *            area
	 */
	public void startSweeping(int from, int to, int lightValuecount, int ultrasonicValuecount) {
		sweepThread.startSweeping(from, to, lightValuecount, ultrasonicValuecount);
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
	 * Move the head manually to a given position
	 * 
	 * @param position
	 *            The new position. Range as given in Javadoc for getPosition()
	 * @param async
	 *            If true, the call immediately returns while the head is still
	 *            moving.
	 * @param speed
	 *            The speed to move with (0<=speed<=1000)
	 */
	public void moveTo(int position, boolean async, int speed) {
		headMotor.moveTo(position, async, speed);
	}

	/**
	 * Return the measured sweep values Lower indices in the array are values
	 * more to the left
	 */
	public int[] getUltrasonicSweepValues() {
		return polledUltrasonicValues;
	}

	/**
	 * Return the measured sweep values Lower indices in the array are values
	 * more to the left
	 */
	public int[] getLightSweepValues() {
		return polledLightValues;
	}

	public int getRawLightValue() {
		return lightSensor.getRawValue();
	}

	/**
	 * Calibrate the light sensor
	 * 
	 * @param minValue
	 *            The value mapped to 0
	 * @param maxValue
	 *            The value mapped to 1000
	 */
	public void calibrateLight(int minValue, int maxValue) {
		lightSensor.calibrate(minValue, maxValue);
	}

	public void resetLightCalibration() {
		lightSensor.resetCalibration();
	}

	public void setSweepSpeed(int speed) {
		sweepThread.setSpeed(speed);
	}
}
