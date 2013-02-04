package strategies.wall_follower.without_sweeping;

import strategies.Strategy;
import strategies.wall_follower.without_sweeping.collision.FollowCollisionStrategy;
import strategies.wall_follower.without_sweeping.edge.EdgeStrategy;
import strategies.wall_follower.without_sweeping.wall.WallFollowerStrategy;
import utils.Utils.Side;
import static robot.Platform.ENGINE;

public class WallFollowerWithoutCollisionController extends Strategy {
	private Side headSide;

	private FollowCollisionStrategy collisionStrategy;
	private EdgeStrategy edgeStrategy;
	private WallFollowerStrategy wallStrategy;

	private enum State {
		START, STARTED, FOLLOW_WALL, WALL_COLLISION, FOLLOW_EDGE
	}

	private State currentState;

	public WallFollowerWithoutCollisionController(Side headSide) {
		this.headSide = headSide;
		collisionStrategy = new FollowCollisionStrategy(headSide, // head,
				5, 90,// detection
				1000,// backward speed
				100, // backward time
				30, // max obstacle distance
				50 // max wall distance
		);
		edgeStrategy = new EdgeStrategy(this.headSide, 50 // wall distance
				, 0, 1000 // Rotation speed, direction
				, 0 // Time
				, 1000, 400);
		wallStrategy = new WallFollowerStrategy(this.headSide);
	}

	private State checkState() {
		collisionStrategy.check();

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
			break;
		case WALL_COLLISION:
			if (collisionStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
			break;
		case FOLLOW_EDGE:
			if (edgeStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
			break;
		}

		// System.out.println("(" + HEAD.getDistance() + ")" + oldState.name()
		// + " -> " + currentState.name());
		return currentState;
	}

	@Override
	protected void doInit() {
		currentState = State.START;

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
				wallStrategy.init();
			}
			edgeStrategy.run();
		case WALL_COLLISION:
			if (collisionStrategy.justStarted()) {
				wallStrategy.init();
			}
			collisionStrategy.run();
		}
	}

}
