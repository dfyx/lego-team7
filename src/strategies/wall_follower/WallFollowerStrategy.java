package strategies.wall_follower;

import static robot.Platform.ENGINE;
import strategies.Strategy;
import strategies.util.MoveDistanceStrategy;
import strategies.wall_follower.collision.FollowCollisionStrategy;
import strategies.wall_follower.edge.EdgeStrategy;
import strategies.wall_follower.wall.WallRegulatorStrategy;
import utils.Utils.Side;

public class WallFollowerStrategy extends Strategy {
	private Side headSide;

	private FollowCollisionStrategy wallCollisionStrategy;
	private FollowCollisionStrategy edgeCollisionStrategy;
	private EdgeStrategy edgeStrategy;
	private WallRegulatorStrategy wallStrategy;
	private MoveDistanceStrategy moveBack;

	private enum State {
		START, STARTED, DRIVE_BACK, FOLLOW_WALL, WALL_COLLISION, FOLLOW_EDGE, EDGE_COLLISION
	}

	private State currentState;

	/**
	 * 
	 * @param headSide
	 * @param rotationTime should be around 300
	 * @param curveSpeed should be 1000
	 * @param curveDirection should be 300 for race and less for labyrinth
	 * @param maxWallDistance used to prevent overregulating after a curve. Should be around 35.
	 * @param desiredWallDistance the desired distance to the wall. Should be around 14.
	 */
	public WallFollowerStrategy(Side headSide, int rotationTime, int curveSpeed, int curveDirection, int maxWallDistance, int desiredWallDistance) {
		this.headSide = headSide;
		wallCollisionStrategy = new FollowCollisionStrategy(headSide, // head
				5, 90,// detection
				500,// backward speed
				1000, // backward time
				30, // max obstacle distance
				400, // obstacle speed
				1000, // obstacle direction
				300, // extra turn time
				maxWallDistance, // max wall distance
				200, // wall speed
				1000 // wall direction
		);
		edgeCollisionStrategy = new FollowCollisionStrategy(headSide, // head
				5, 90,// detection
				500,// backward speed
				1000, // backward time
				30, // max obstacle distance
				400, // obstacle speed
				1000, // obstacle direction
				300, // extra turn time
				50, // max wall distance
				200, // wall speed
				1000 // wall direction
		);
//		edgeCollisionStrategy = new EdgeCollisionStrategy(headSide, //head
//				5, 90, // detection
//				500, // backward speed
//				1000 // backward time
//				);
		edgeStrategy = new EdgeStrategy(this.headSide, maxWallDistance // wall distance
				, 1000, 1000 // Rotation speed, direction
				, rotationTime // Time
				, curveSpeed, curveDirection);
		wallStrategy = new WallRegulatorStrategy(this.headSide, 500, desiredWallDistance);
		moveBack = new MoveDistanceStrategy();
		moveBack.init();
		moveBack.setSpeed(1000);
		moveBack.setTargetPosition(-200);
	}

	private State checkState() {
		wallCollisionStrategy.check();
		edgeCollisionStrategy.check();
		edgeStrategy.check();

		State oldState = currentState;
		switch (currentState) {
		case START:
			currentState = State.DRIVE_BACK;
			break;
		case DRIVE_BACK:
			if(moveBack.isFinished())
				currentState = State.STARTED;
			break;
		case STARTED:
			currentState = State.FOLLOW_WALL;
			break;
		case FOLLOW_WALL:
			if (wallCollisionStrategy.willStart())
				currentState = State.WALL_COLLISION;
			else if (edgeStrategy.willStart())
				currentState = State.FOLLOW_EDGE;
			break;
		case WALL_COLLISION:
			if (wallCollisionStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
			break;
		case FOLLOW_EDGE:
			// TODO SB correct order?
			if (edgeCollisionStrategy.willStart())
				currentState = State.EDGE_COLLISION;
			else if (edgeStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
			break;
		case EDGE_COLLISION:
			if (edgeCollisionStrategy.isStopped())
				currentState = State.FOLLOW_EDGE;
			break;
		}

		if (oldState != currentState)
			System.out.println(oldState.name() + " -> " + currentState.name());
		return currentState;
	}

	@Override
	protected void doInit() {
		currentState = State.START;

		wallCollisionStrategy.init();
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
		case DRIVE_BACK:
			moveBack.run();
			break;
		case STARTED:
			ENGINE.move(1000, 0);
			break;
		case FOLLOW_WALL:
			if (wallStrategy.justStarted()) {
				wallCollisionStrategy.init();
				edgeStrategy.init();
			}
			wallStrategy.run();
			break;
		case FOLLOW_EDGE:
			if (edgeStrategy.justStarted()) {
				wallStrategy.init();
				edgeCollisionStrategy.init();
			}
			edgeStrategy.run();
			break;
		case WALL_COLLISION:
			if (wallCollisionStrategy.justStarted()) {
				wallStrategy.init();
			}
			wallCollisionStrategy.run();
			break;
		case EDGE_COLLISION:
			if (edgeCollisionStrategy.justStarted()) {
				edgeStrategy.init();
			}
			edgeCollisionStrategy.run();
			break;
		}
	}

}
