package sensors;

import robot.Platform;
import strategies.Action;

public class Head implements Action {

	private static final UltrasonicSensor ultrasonicSensor = new UltrasonicSensor(
			Platform.ULTRASONIC_PORT);
	private static final LightSensor lightSensor = new LightSensor(
			Platform.LIGHT_PORT);

	// Last polled sensor values
	private int polledDistance;
	private int polledLight;
	private int polledPosition;

	private HeadMotor headMotor = new HeadMotor();
	private SweepAction sweepAction = new SweepAction(headMotor);

	public void poll() {
		polledPosition = headMotor.getPosition();
		polledDistance = ultrasonicSensor.getValue();
		polledLight = lightSensor.getValue();
	}

	public int getDistance() {
		return polledDistance;
	}

	@Override
	public void run() {
		sweepAction.run();
		headMotor.run();
	}

	/**
	 * Returns the light value, normalized between 0 and 1000. Backlight effects
	 * are suppressed.
	 */
	public int getLight() {
		return polledLight;
	}

	public boolean isSweeping() {
		return sweepAction.isRunning();
	}

	public void stopMoving() {
		sweepAction.stopSweeping();
		headMotor.stopMoving();
	}

	public void terminate() {
		sweepAction.terminate();
		headMotor.terminate();
	}

	/**
	 * Switch floodlight on or off
	 */
	public void setFloodlight(boolean value) {
		lightSensor.setFloodlight(value);
	}

	/**
	 * Start collision detection. You need to explicitly stop this befor moving
	 * the head.
	 * 
	 * @param detect
	 *            True, iff collisions should be detected. False otherwise.
	 */
	public void detectCollisions(boolean detect) {
		headMotor.detectCollisions(detect);
	}

	/**
	 * Ensure, that the motor is floating, when calling this method. Otherwise
	 * it will always return false; If the head is moving, while calling this
	 * method, the behaviour is undefined.
	 * 
	 * @return True, iff the head is turning due to a collision.
	 */
	public boolean isColliding() {
		return headMotor.isColliding();
	}

	/**
	 * Returns true, iff the sensor head is currently moving
	 */
	public boolean isMoving() {
		return isSweeping() || headMotor.isMoving();
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
		sweepAction.stopSweeping();
	}

	/**
	 * Start sweeping
	 * 
	 * @param from
	 *            The leftmost position to sweep. See getPosition() for range.
	 * @param to
	 *            The rightmost position to sweep. See getPosition() for range.
	 * @param speed
	 *            The speed to sweep with (0<=speed<=1000)
	 */
	public void startSweeping(int from, int to, int speed) {
		sweepAction.startSweeping(from, to, speed);
	}

	/**
	 * Move the head manually to a given position. Don't use this function while
	 * sweeping! (after stopSweeping() wait until isSweeping()==false, before
	 * you call this function)
	 * 
	 * @param position
	 *            The new position. Range as given in Javadoc for getPosition()
	 * @param async
	 *            If true, the call immediately returns while the head is still
	 *            moving.
	 * @param speed The speed to move with (0<=speed<=1000)
	 */
	public void moveTo(int position, int speed) {
		if (isSweeping())
			throw new IllegalStateException("moveTo() call while sweeping");
		headMotor.moveTo(position, speed);
	}

	public int getRawLightValue() {
		return lightSensor.getRawValue();
	}

	/**
	 * Calibrate the light sensor.
	 * The passed values must have been obtained by
	 * {@link #getLight()} because of the backlight-subtraction applied there.

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

	/**
	 * Converts an angle in degress to a positional value compliant with
	 * {@link #getPosition()}.
	 * 
	 * @param degrees
	 *            an angle, {@code -90 <= angle <= 90}
	 * @return a position value, {@code -1000 <= result <= 1000}
	 */
	public static int degreeToPosition(final int degrees) {
		return (degrees * 1000) / 90;
	}

	/**
	 * Converts a positional value as provided by {@link #getPosition()} to an
	 * angular value.
	 * 
	 * @param position
	 *            a position value, {@code -1000 <= position <= 1000}
	 * @return an angle, {@code -90 <= result <= 90}
	 */
	public static int positionToDegrees(final int position) {
		return (position * 90) / 1000;
	}
}
