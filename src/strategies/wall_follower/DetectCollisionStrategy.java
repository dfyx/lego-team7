package strategies.wall_follower;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import strategies.util.ChildStrategy;
import utils.Utils.Side;

/**
 * Initialization of this class is blocking! :-(
 * 
 * @author sebastian
 * 
 */
public class DetectCollisionStrategy extends ChildStrategy {
	private boolean foundWall;

	Side headSide;
	
	private static final int HEAD_POSITION = 850;

	/**
	 * Detect collisions.
	 * 
	 */
	public DetectCollisionStrategy(Side headSide) {
		this.headSide = headSide;
	}

	private enum State {
		START, START_TURN_HEAD, TURNING_HEAD, START_DRIVING, DRIVING, COLLISION
	}

	private static State currentState;

	@Override
	protected void childInit() {
		HEAD.moveTo(headSide.getValue() * 800, 1000);
		currentState = State.START;
		foundWall = false;
	} // TODO SB exception, if not floating

	private State checkState() {
		State newState = currentState;
		switch (currentState) {
		case START:
			newState = State.START_TURN_HEAD;
			break;
		case START_TURN_HEAD:
			newState = State.TURNING_HEAD;
			break;
		case TURNING_HEAD:
			if (!HEAD.isMoving())
				newState = State.START_DRIVING;
			break;
		case START_DRIVING:
			newState = State.DRIVING;
			break;
		case DRIVING:
			if (HEAD.isColliding())
				newState = State.COLLISION;
			break;
		case COLLISION:
			break;
		}
		// System.out.println("State: " + newState);
		return newState;
	}

	@Override
	public void check() {
		currentState = checkState();

		switch (currentState) {
		case START:
			break;
		case START_TURN_HEAD:
			HEAD.moveTo(HEAD_POSITION*headSide.getValue(), 1000);
			break;
		case TURNING_HEAD:
			break;
		case START_DRIVING:
			HEAD.detectCollisions(true);
			break;
		case DRIVING:
			break;
		case COLLISION:
			foundWall = true;
			break;
		}
	}

	@Override
	public boolean willStart() {
		return foundWall;
	}

	@Override
	public boolean isStopped() {
		return true;
	}

	@Override
	public void work() {
		HEAD.detectCollisions(false);
		System.out.println("stopping engine");
		ENGINE.stop();
	}

}
