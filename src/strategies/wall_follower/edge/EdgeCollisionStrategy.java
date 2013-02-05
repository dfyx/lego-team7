package strategies.wall_follower.edge;

import static robot.Platform.ENGINE;
import strategies.util.ChildStrategy;
import strategies.wall_follower.DetectCollisionStrategy;
import utils.Utils.Side;

public class EdgeCollisionStrategy extends ChildStrategy {
	DetectCollisionStrategy collisionStrategy;
	
	private final int BACKWARD_SPEED;
	private final long BACKWARD_TIME;
	private long endBackwardTime;

	private enum State {
		START, // run is only called, if collision occurred
		COLLIDE, STAND, DRIVE_BACK, // drive away from wall
		FINISHED // try curve again
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
				newState = State.FINISHED;
			break;
		case FINISHED:
			break;
		}

//		if (currentState != newState)
//			System.out.println(currentState.name() + " -> " + newState.name());

		return newState;
	}

	public EdgeCollisionStrategy(Side headSide, int valueCount,
			int sensitivity, int backwardSpeed, int backwardTime) {
		collisionStrategy = new DetectCollisionStrategy(headSide);
		BACKWARD_SPEED = -backwardSpeed;
		BACKWARD_TIME = backwardTime;
	}

	@Override
	public boolean willStart() {
		return collisionStrategy.willStart();
	}

	@Override
	public boolean isStopped() {
		return currentState == State.FINISHED;
	}

	@Override
	protected void childInit() {
		System.out.println("init edge collision");
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
		case FINISHED:
			// TODO SB necessary
			ENGINE.stop();
			break;
		}
	}
}
