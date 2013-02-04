package strategies.wall_follower.without_sweeping.collision;

import static robot.Platform.HEAD;
import static robot.Platform.ENGINE;
import strategies.util.ChildStrategy;
import strategies.wall_follower.without_sweeping.DetectCollisionStrategy;
import utils.Utils.Side;

public class FollowCollisionStrategy extends ChildStrategy {
	// TODO SB make this work for both head positions
	DetectCollisionStrategy collisionStrategy;

	private final int EPSILON = 5;
	
	private final int BACKWARD_SPEED;
	private final long BACKWARD_TIME;
	private long endBackwardTime;

	private final int FRONT_POSITION = 0;
	private final int SIDE_POSITION;

	private final int SEARCH_OBSTACLE_SPEED;
	private final int SEARCH_OBSTACLE_DIRECTION;
	private final int OBSTACLE_DISTANCE;

	private final int SEARCH_WALL_SPEED;
	private final int SEARCH_WALL_DIRECTION;
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
		START_TURN_HEAD_SIDEWAYS, // start turning head
		TURN_HEAD_SIDEWAYS, // head turning to wall (on right side)
		START_SEARCH_WALL, // start turning robot
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
			newState = State.SEARCH_OBSTACLE;
			break;
		case SEARCH_OBSTACLE:
			if (!HEAD.isMoving() && HEAD.getPosition() <= FRONT_POSITION + EPSILON && HEAD.getPosition() >= FRONT_POSITION - EPSILON)
				newState = State.AVOID_OBSTACE;
			break;
		case AVOID_OBSTACE:
			if (HEAD.getDistance() > OBSTACLE_DISTANCE)
				newState = State.TURN_HEAD_SIDEWAYS;
			break;
		case START_TURN_HEAD_SIDEWAYS:
			newState = State.TURN_HEAD_SIDEWAYS;
			break;
		case TURN_HEAD_SIDEWAYS:
			if (!HEAD.isMoving() && HEAD.getPosition() <= SIDE_POSITION + EPSILON && HEAD.getPosition() >= SIDE_POSITION - EPSILON)
				newState = State.SEARCH_WALL;
			break;
		case START_SEARCH_WALL:
			newState = State.SEARCH_WALL;
			break;
		case SEARCH_WALL:
			if (HEAD.getDistance() < WALL_DISTANCE)
				newState = State.WALL_FOUND;
			break;
		case WALL_FOUND:
			break;
		}

		if (currentState != newState)
			System.out.println(currentState.name() + " -> " + newState.name());

		return newState;
	}

	public FollowCollisionStrategy(Side headSide, int valueCount,
			int sensitivity, int backwardSpeed, int backwardTime,
			int obstacleDistance, int searchObstacleSpeed,
			int searchObstacleDirection, int wallDistance, int searchWallSpeed,
			int searchWallDirection) {
		SIDE_POSITION = 1000 * headSide.getValue();

		collisionStrategy = new DetectCollisionStrategy(valueCount, sensitivity);
		BACKWARD_SPEED = -backwardSpeed;
		BACKWARD_TIME = backwardTime;

		OBSTACLE_DISTANCE = obstacleDistance;
		SEARCH_OBSTACLE_SPEED = searchObstacleSpeed;
		SEARCH_OBSTACLE_DIRECTION = searchObstacleDirection
				* -headSide.getValue();

		WALL_DISTANCE = wallDistance;
		SEARCH_WALL_SPEED = searchWallSpeed;
		SEARCH_WALL_DIRECTION = searchWallDirection * headSide.getValue();// TODO
																			// SB
																			// correct?
	}

	@Override
	public boolean willStart() {
		return collisionStrategy.willStart();
	}

	@Override
	public boolean isStopped() {
		// TODO SB return currentState == State.WALL_FOUND;
		return false;
	}

	@Override
	protected void childInit() {
		System.out.println("init collision");
		currentState = State.START;
		collisionStrategy.init();
	}

	@Override
	public void check() {
		collisionStrategy.check();
	}

	@Override
	public void work() {

		State oldState = currentState;
		currentState = checkState();
		if (oldState != currentState)
			System.out.println("running: " + currentState.name());

		switch (currentState) {
		case START:
			break;
		case COLLIDE:
			collisionStrategy.run();
			break;
		case STAND:
			ENGINE.move(BACKWARD_SPEED, 0);
			break;
		case DRIVE_BACK:
			// intentionally left blank
			// TODO SB use touch sensors?
			break;
		case TURN_HEAD_FORWARD:
			ENGINE.stop();
			HEAD.moveTo(FRONT_POSITION, true);
			break;
		case SEARCH_OBSTACLE:
			// Turning head forward
			break;
		case AVOID_OBSTACE:
			// head turned forward. start turning
			ENGINE.move(SEARCH_OBSTACLE_SPEED, SEARCH_OBSTACLE_DIRECTION);
			break;
		case START_TURN_HEAD_SIDEWAYS:
			// Start turning head sideways
			ENGINE.stop();
			HEAD.moveTo(SIDE_POSITION, true);
			break;
		case TURN_HEAD_SIDEWAYS:
			// head turning sideways
			break;
		case START_SEARCH_WALL:
			// head turned sideways. start turning.
			ENGINE.move(SEARCH_WALL_SPEED, SEARCH_WALL_DIRECTION);
			break;
		case SEARCH_WALL:
			// turning
			break;
		case WALL_FOUND:
			// TODO SB necessary?
			ENGINE.stop();
			break;
		}
	}
}
