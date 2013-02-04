package strategies.wall_follower.without_sweeping.collision;

import strategies.util.ChildStrategy;
import strategies.wall_follower.without_sweeping.DetectCollisionStrategy;

public class FollowCollisionStrategy extends ChildStrategy {
	// TODO SB make this work for both head positions
	DetectCollisionStrategy collisionStrategy;

	private final int BACKWARD_SPEED;
	private final long BACKWARD_TIME;
	private long endBackwardTime;

	// TODO SM (SB) check if collision was false positive through turning head
	// to front and measuring resistance
	private enum State {
		START, // run is only called, if collision occured
		COLLIDE, STAND, DRIVE_BACK, //
		TURN_HEAD_FORWARD, // turn head to front
		SEARCH_OBSTACLE, // look forward, if obstacle is there
		AVOID_OBSTACE, // turn left until obstacle goes out of side
		// obstacle out of sight -> turn head to wall
		TURN_HEAD_SIDEWAYS, // turn head to wall (on right side)
		SEARCH_WALL, // turn left until wall is found
		WALL_FOUND
	}

	private State currentState;

	private State checkState() {
		State newState = currentState;

		switch (newState) {
		case START:
			newState = State.COLLIDE;
			break;
		case COLLIDE:
			newState = State.STAND;
			break;
		case STAND:
			newState = State.DRIVE_BACK;
			endBackwardTime = System.currentTimeMillis() + BACKWARD_TIME;
			break;
		case DRIVE_BACK:
			if (System.currentTimeMillis() > endBackwardTime)
				newState = State.TURN_HEAD_FORWARD;
			break;
		case TURN_HEAD_FORWARD:
			if (headIsForward)
				newState = State.SEARCH_OBSTACLE;
			break;
		case SEARCH_OBSTACLE:
			newState = State.AVOID_OBSTACE;
			break;
		case AVOID_OBSTACE:
			if (noObstacleInSight)
				newState = State.TURN_HEAD_SIDEWAYS;
			break;
		case TURN_HEAD_SIDEWAYS:
			if (headIsSideways)
				newState = State.SEARCH_WALL;
			break;
		case SEARCH_WALL:
			if (wallFound)
				newState = State.WALL_FOUND;
			break;
		case WALL_FOUND:
			break;
		}

		return newState;
	}

	public FollowCollisionStrategy(int valueCount, int sensitivity, int backwardSpeed, int backwardTime) {
		collisionStrategy = new DetectCollisionStrategy(valueCount, sensitivity);
		BACKWARD_SPEED = backwardSpeed;
		BACKWARD_TIME = backwardTime;
	}

	@Override
	public boolean willStart() {
		return collisionStrategy.willStart();
	}

	@Override
	public boolean isStopped() {
		// TODO SB only used to stay stopped
		return false;
	}

	@Override
	protected void childInit() {
		currentState = State.START;
		collisionStrategy.init();
	}

	@Override
	public void check() {
		collisionStrategy.check();
	}

	@Override
	public void work() {
		collisionStrategy.work();
	}

}
