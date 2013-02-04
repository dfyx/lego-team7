package strategies.wall_follower.without_sweeping.collision;

import static robot.Platform.HEAD;
import strategies.util.ChildStrategy;
import strategies.wall_follower.without_sweeping.DetectCollisionStrategy;
import utils.Utils.Side;

public class FollowCollisionStrategy extends ChildStrategy {
	// TODO SB make this work for both head positions
	DetectCollisionStrategy collisionStrategy;

	private final int BACKWARD_SPEED;
	private final long BACKWARD_TIME;
	private long endBackwardTime;

	private final int FRONT_POSITION = 0;
	private final int SIDE_POSITION;

	private final int OBSTACLE_DISTANCE;

	private final int WALL_DISTANCE;

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
			if (!HEAD.isMoving() && HEAD.getPosition() == FRONT_POSITION)
				newState = State.SEARCH_OBSTACLE;
			break;
		case SEARCH_OBSTACLE:
			newState = State.AVOID_OBSTACE;
			break;
		case AVOID_OBSTACE:
			if (HEAD.getDistance() > OBSTACLE_DISTANCE)
				newState = State.TURN_HEAD_SIDEWAYS;
			break;
		case TURN_HEAD_SIDEWAYS:
			if (!HEAD.isMoving() && HEAD.getPosition() == SIDE_POSITION)
				newState = State.SEARCH_WALL;
			break;
		case SEARCH_WALL:
			if (HEAD.getDistance() < WALL_DISTANCE)
				newState = State.WALL_FOUND;
			break;
		case WALL_FOUND:
			break;
		}

		return newState;
	}

	public FollowCollisionStrategy(Side headSide, int valueCount,
			int sensitivity, int backwardSpeed, int backwardTime,
			int obstaclePosition, int wallDistance) {
		SIDE_POSITION = 1000 * headSide.getValue();

		collisionStrategy = new DetectCollisionStrategy(valueCount, sensitivity);
		BACKWARD_SPEED = backwardSpeed;
		BACKWARD_TIME = backwardTime;

		OBSTACLE_DISTANCE = obstaclePosition;
		WALL_DISTANCE = wallDistance;
	}

	@Override
	public boolean willStart() {
		return collisionStrategy.willStart();
	}

	@Override
	public boolean isStopped() {
		return currentState == State.WALL_FOUND;
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
		State currentState = checkState();
		switch (currentState) {
		case START:
			break;
		case COLLIDE:
			break;
		case STAND:
			break;
		case DRIVE_BACK:
			break;
		case TURN_HEAD_FORWARD:
			break;
		case SEARCH_OBSTACLE:
			break;
		case AVOID_OBSTACE:
			break;
		case TURN_HEAD_SIDEWAYS:
			break;
		case SEARCH_WALL:
			break;
		case WALL_FOUND:
			break;
		}
		// TODO SB collisionStrategy.work();
	}

}
