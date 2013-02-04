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
		collisionStrategy = new FollowCollisionStrategy(headSide, // head
				5, 90,// detection
				500,// backward speed
				1000, // backward time
				30, // max obstacle distance
				200, // obstacle speed
				1000, // obstacle direction
				50, // max wall distance
				200, // wall speed
				1000 // wall direction
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

//		if (oldState != currentState)
//			System.out.println(oldState.name() + " -> " + currentState.name());
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
		State oldState = currentState;
		currentState = checkState();
		if(oldState != currentState)
			System.out.println("running: " + currentState.name());

		switch (currentState) {
		case STARTED:
			ENGINE.move(1000, 0);
			break;
		case FOLLOW_WALL:
			if (wallStrategy.justStarted()) {
				collisionStrategy.init();
				edgeStrategy.init();
			} else if(wallStrategy.badValues()) {
				// pause collision detection, if we are not driving in a straight line
				collisionStrategy.init();
				System.out.println("pause collision");
			} else {
				System.out.println("use collision");
			}
			wallStrategy.run();
			break;
		case FOLLOW_EDGE:
			if (edgeStrategy.justStarted()) {
				wallStrategy.init();
			}
			edgeStrategy.run();
			break;
		case WALL_COLLISION:
			if (collisionStrategy.justStarted()) {
				wallStrategy.init();
			}
			collisionStrategy.run();
			break;
		}
	}

}
