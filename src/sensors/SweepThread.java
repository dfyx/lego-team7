package sensors;

import strategies.Action;

public class SweepThread implements Action {

	private enum State {
		INACTIVE, SHOULD_START, SWEEP_LEFT, SWEEP_RIGHT
	};

	private State state;

	private HeadMotor motor;

	private volatile int sweepFrom;
	private volatile int sweepTo;
	private volatile int sweepSpeed;

	public SweepThread(HeadMotor motor) {
		this.motor = motor;
		state = State.INACTIVE;
	}

	public void terminate() {
		motor.terminate();
		state = State.INACTIVE;
	}

	public boolean isRunning() {
		return state != State.INACTIVE;
	}

	/**
	 * Start sweeping
	 * 
	 * @param from
	 *            The leftmost x position to sweep. See HeadMotor.getPosition()
	 *            for range.
	 * @param to
	 *            The rightmost x position to sweep. See HeadMotor.getPosition()
	 *            for range.
	 * @param speed
	 *            The speed to move with (0<=speed<=1000)
	 */
	public void startSweeping(int from, int to, int speed) {
		switch (state) {
		case SWEEP_LEFT:
		case SWEEP_RIGHT:
			motor.stopMoving();
			//no break !
		case INACTIVE:
		case SHOULD_START:
			this.sweepFrom = from;
			this.sweepTo = to;
			this.sweepSpeed = speed;
			this.state = State.SHOULD_START;
			break;
		}
	}

	public void stopSweeping() {
		motor.stopMoving();
		state = State.INACTIVE;
	}

	@Override
	public void run() {
		switch (state) {
		case INACTIVE:
			break;
		case SHOULD_START:
			motor.moveTo(sweepFrom, 1000);
			state = State.SWEEP_LEFT;
			break;
		case SWEEP_LEFT:
			if (!motor.isMoving()) {
				motor.moveTo(sweepTo, sweepSpeed);
				state = State.SWEEP_RIGHT;
			}
			break;
		case SWEEP_RIGHT:
			if (!motor.isMoving()) {
				motor.moveTo(sweepFrom, sweepSpeed);
				state = State.SWEEP_LEFT;
			}
			break;
		}
	}
}
