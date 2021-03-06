package strategies.wall_follower.edge;

import strategies.util.ChildStrategy;
import utils.Utils.Side;
import static robot.Platform.HEAD;
import static robot.Platform.ENGINE;

public class EdgeStrategy extends ChildStrategy {
	private final int wallDistance;

	private int currentDistance = 0;
	private int lastDistance = 0;

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
//		if (newState != currentState)
//			System.out.println(currentState.name() + " -> " + newState.name());
		return newState;
	}

	@Override
	public void work() {

//		State oldState = currentState;
		currentState = checkState();
//		if (oldState != currentState)
//			System.out.println("running: " + currentState.name());

		switch (currentState) {
		case START:
			break;
		case START_TURN:
			endTurnTime = System.currentTimeMillis() + ROTATION_TIME;
//			System.out.println("start turning: " + System.currentTimeMillis()
//					+ " / " + (System.currentTimeMillis() + ROTATION_TIME));
			ENGINE.move(ROTATION_SPEED, ROTATION_DIRECTION);
			break;
		case TURNING:
//			System.out.println("turning");
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
		this.wallDistance = wallDistance;
		this.ROTATION_SPEED = rotationSpeed;
		this.ROTATION_DIRECTION = -headSide.getValue() * rotationDirection;
		this.ROTATION_TIME = rotationTime;

		this.CURVE_SPEED = curveSpeed;
		this.CURVE_DIRECTION = headSide.getValue() * curveDirection;
	}

	@Override
	public boolean willStart() {
		return currentDistance >= wallDistance && lastDistance >= wallDistance;
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
		lastDistance = currentDistance;
		currentDistance = HEAD.getDistance();
		return;
	}

}
