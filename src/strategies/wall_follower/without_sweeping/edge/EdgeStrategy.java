package strategies.wall_follower.without_sweeping.edge;

import strategies.util.ChildStrategy;
import utils.Utils.Side;
import static robot.Platform.HEAD;
import static robot.Platform.ENGINE;

public class EdgeStrategy extends ChildStrategy {
	private Side headSide;

	private final int wallDistance;

	/**
	 * Time to turn
	 */
	private final int ROTATION_TIME;
	/**
	 * Speed and direction of the rotation at the start of the curve
	 */
	private final int ROTATION_SPEED;
	private final int ROTATION_DIRECTION;
	/**
	 * Speed and direction during the turn
	 */
	private final int CURVE_SPEED;
	private final int CURVE_DIRECTION;
	private long endTurnTime;

	private enum State {
		START, START_TURN, TURNING, START_CURVE, CURVING
	}

	State currentState;

	private State checkState() {
		State newState = currentState;
		switch (currentState) {
		case START:
			newState = State.START_TURN;
			break;
		case START_TURN:
			newState = State.TURNING;
			break;
		case TURNING:
			if (System.currentTimeMillis() > endTurnTime)
				newState = State.START_CURVE;
			break;
		case START_CURVE:
			newState = State.CURVING;
			break;
		case CURVING:
			break;
		}
		return newState;
	}

	@Override
	public void work() {
		currentState = checkState();
		switch (currentState) {
		case START:
			break;
		case START_TURN:
			endTurnTime = System.currentTimeMillis() + ROTATION_TIME;
			System.out.println("start turning: " + System.currentTimeMillis()
					+ " / " + (System.currentTimeMillis() + ROTATION_TIME));
			ENGINE.move(ROTATION_SPEED, ROTATION_DIRECTION);
			break;
		case TURNING:
			System.out.println("turning");
			break;
		case START_CURVE:
			// TODO SB use head position
			ENGINE.move(CURVE_SPEED, CURVE_DIRECTION);
		case CURVING:
			// TODO SB collision detection
			break;
		}
	}

	/**
	 * 
	 * @param wallDistance
	 *            Distance (in cm) which the next wall should be away, to detect
	 *            an edge. Should be around 55;
	 * @param rotationTime
	 *            Time to rotation (in ms)
	 */
	public EdgeStrategy(Side headSide, int wallDistance, int rotationSpeed,
			int rotationDirection, int rotationTime, int curveSpeed,
			int curveDirection) {
		this.headSide = headSide;

		this.wallDistance = wallDistance;
		this.ROTATION_SPEED = rotationSpeed;
		if (this.headSide == Side.RIGHT)
			this.ROTATION_DIRECTION = -rotationDirection;
		else
			this.ROTATION_DIRECTION = rotationDirection;
		this.ROTATION_TIME = rotationTime;

		this.CURVE_SPEED = curveSpeed;
		if (this.headSide == Side.RIGHT)
			this.CURVE_DIRECTION = curveDirection;
		else
			this.CURVE_DIRECTION = -curveDirection;
	}

	@Override
	public boolean willStart() {
		return HEAD.getDistance() >= wallDistance;
	}

	@Override
	public boolean isStopped() {
		return !willStart()
				&& (currentState == State.START_CURVE || currentState == State.CURVING);
	}

	@Override
	protected void childInit() {
		currentState = State.START;
	}

	@Override
	public void check() {
		return;
	}

}
