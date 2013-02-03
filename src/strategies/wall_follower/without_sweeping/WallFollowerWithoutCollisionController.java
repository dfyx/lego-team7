package strategies.wall_follower.without_sweeping;

import strategies.Strategy;
import strategies.wall_follower.without_sweeping.collision.FollowCollisionStrategy;
import strategies.wall_follower.without_sweeping.edge.EdgeStrategy;
import strategies.wall_follower.without_sweeping.wall.WallFollowerStrategy;
import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;

public class WallFollowerWithoutCollisionController extends Strategy {
	private FollowCollisionStrategy collisionStrategy;
	private EdgeStrategy edgeStrategy;
	private WallFollowerStrategy wallStrategy;

	private enum State {
		START, STARTED, FOLLOW_WALL, WALL_COLLISION, FOLLOW_EDGE
	}

	private State currentState;

	public WallFollowerWithoutCollisionController() {
		collisionStrategy = new FollowCollisionStrategy();
		edgeStrategy = new EdgeStrategy();
	}

	private State checkState() {
		State oldState = currentState;
		switch (currentState) {
		case START:
			currentState = State.STARTED;
			break;
		case STARTED:
			currentState = State.FOLLOW_WALL;
			break;
		case FOLLOW_WALL:
			if (collisionStrategy.willStart())
				currentState = State.WALL_COLLISION;
			else if (edgeStrategy.willStart())
				currentState = State.FOLLOW_EDGE;
		case WALL_COLLISION:
			if (collisionStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
		case FOLLOW_EDGE:
			if (edgeStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
		}

		System.out.println("(" + HEAD.getDistance() + ")" + oldState.name()
				+ " -> " + currentState.name());
		return currentState;
	}

	@Override
	protected void doInit() {
		currentState = State.START;
		collisionStrategy = new FollowCollisionStrategy();
		edgeStrategy = new EdgeStrategy();
		wallStrategy = new WallFollowerStrategy();

		collisionStrategy.init();
		edgeStrategy.init();
		wallStrategy.init();
	}

	@Override
	protected void doRun() {
		currentState = checkState();

		switch (currentState) {
		case STARTED:
			ENGINE.move(1000, 0);
			break;
		case FOLLOW_WALL:
			if (wallStrategy.justStarted()) {
				collisionStrategy.init();
				edgeStrategy.init();
			}
			wallStrategy.run();
			break;
		case FOLLOW_EDGE:
			if (edgeStrategy.justStarted()) {

			}
			edgeStrategy.run();
		case WALL_COLLISION:
			if (collisionStrategy.justStarted()) {

			}
			collisionStrategy.run();
		}
	}

}
