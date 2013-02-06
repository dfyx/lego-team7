package strategies.wall_follower;

import static robot.Platform.ENGINE;
import strategies.Strategy;
import strategies.wall_follower.collision.FollowCollisionStrategy;
import strategies.wall_follower.edge.EdgeStrategy;
import strategies.wall_follower.find_wall.FindWallStrategy;
import strategies.wall_follower.wall.WallRegulatorStrategy;
import utils.Utils.Side;

public class WallFollowerStrategy extends Strategy {
	private Side headSide;
	
	private int wallCollisionCount = 0;
	private int lostEdgeCount = 0;
	
	/**
	 * May be used to detect turns. (unsafe)
	 * Use #init to reset.
	 * @return 
	 */
	public int getWallCollisionCount() {
		return wallCollisionCount;
	}
	
	/**
	 * May be used to detect turns. (unsafe)
	 * Use # init to reset.
	 * @return
	 */
	public int getLostEdgeCount() {
		return lostEdgeCount;
	}

	private FollowCollisionStrategy wallCollisionStrategy;
	private FollowCollisionStrategy edgeCollisionStrategy;
	private EdgeStrategy edgeStrategy;
	private WallRegulatorStrategy wallStrategy;
	private FindWallStrategy startStrategy;

	private enum State {
		START, STARTED, FIND_WALL, FOLLOW_WALL, WALL_COLLISION, FOLLOW_EDGE, EDGE_COLLISION
	}

	private State currentState;
	
	public static WallFollowerStrategy getSliderStrategy(Side headSide, int desiredDistance) {
		return new WallFollowerStrategy(headSide, // side
				0, // rotation time
				1000, // curve speed
				470, // curve direction
				35, // max wall distance
				10 // desired wall distance
		);
	}
	
	public static WallFollowerStrategy getMazeStrategy(Side headSide) {
		return new WallFollowerStrategy(headSide, // side
				0, // rotation time
				1000, // curve speed
				470, // curve direction
				35, // max wall distance
				14 // desired wall distance
		);
	}
	
	public static WallFollowerStrategy getRaceStrategy() {
		return new WallFollowerStrategy(Side.LEFT, // side
				0, // rotation time
				1000, // curve speed
				470, // curve direction
				35, // max wall distance
				14 // desired wall distance
		);
	}

	/**
	 * 
	 * @param headSide
	 * @param rotationTime should be around 300
	 * @param curveSpeed should be 1000
	 * @param curveDirection should be 300 for race and less for labyrinth
	 * @param maxWallDistance used to prevent overregulating after a curve. Should be around 35.
	 * @param desiredWallDistance the desired distance to the wall. Should be around 14.
	 */
	private WallFollowerStrategy(Side headSide, int rotationTime, int curveSpeed, int curveDirection, int maxWallDistance, int desiredWallDistance) {
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
		startStrategy = new FindWallStrategy(headSide, 1000, -200, 15, 30, 500, 1000, 1000);
	}

	private State checkState() {
		wallCollisionStrategy.check();
		edgeCollisionStrategy.check();
		edgeStrategy.check();

		State oldState = currentState;
		switch (currentState) {
		case START:
			currentState = State.FIND_WALL;
			break;
		case FIND_WALL:
			if(startStrategy.isStopped())
				currentState = State.STARTED;
			break;
		case STARTED:
			currentState = State.FOLLOW_WALL;
			break;
		case FOLLOW_WALL:
			if (wallCollisionStrategy.willStart()) {
				currentState = State.WALL_COLLISION;
				wallCollisionCount++;
			} else if (edgeStrategy.willStart()) {
				currentState = State.FOLLOW_EDGE;
				lostEdgeCount++;
			}
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
		startStrategy.init();
		
		wallCollisionCount = 0;
		lostEdgeCount = 0;
	}

	@Override
	protected void doRun() {
		State oldState = currentState;
		currentState = checkState();
		if(oldState != currentState)
			System.out.println("running: " + currentState.name());

		switch (currentState) {
		case FIND_WALL:
			startStrategy.run();
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
