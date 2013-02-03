package strategies.wall_follower.without_sweeping;

import strategies.Strategy;
import static robot.Platform.ENGINE;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;

public class WallFollowerWithoutCollisionController extends Strategy {

	private enum State {
		START, STARTED, FOLLOW_WALL, WALL_COLLISION, FOLLOW_EDGE
	}

	private State currentState;

	private State checkState() {
		switch(currentState) {
		case START:
			currentState = State.STARTED;
			break;
		case STARTED:
			currentState = State.FOLLOW_WALL;
			break;
		case FOLLOW_WALL:
			if(collisionStrategy.willStart())
				currentState = State.WALL_COLLISION;
			else if(edgeStrategy.willStart())
				currentState = State.FOLLOW_EDGE;
		case WALL_COLLISION:
			if(collisionStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
		case FOLLOW_EDGE:
			if(edgeStrategy.isStopped())
				currentState = State.FOLLOW_WALL;
		}
		return currentState;
	}

	@Override
	protected void doInit() {
		currentState = State.START;
	}

	@Override
	protected void doRun() {
		// TODO Auto-generated method stub
	}

}
