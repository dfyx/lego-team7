package strategies.sections;

import static robot.Platform.HEAD;
import strategies.util.StateMachineStrategy;
import strategies.wall_follower.WallFollowerStrategy;
import utils.Utils;
import utils.Utils.Side;

public class RaceStrategy extends StateMachineStrategy<RaceStrategy.State> {

	protected enum State {
		INIT, SEARCH_LEFT_WALL, MEASURE_LEFT_WALL, SEARCH_RIGHT_WALL, MEASURE_RIGHT_WALL, WAIT, RACE
	}

	private final static int WAIT_TIME = 10000;

	public RaceStrategy() {
		super(State.INIT);
	}

	private int leftValue;
	private int rightValue;
	private WallFollowerStrategy wallFollowerStrategy;
	private int startTime;

	@Override
	protected State run(State currentState) {
		System.out.println("--run");
		System.out.println("State: "+currentState);
		State newState = currentState;
		
		switch (currentState) {
		case INIT:
			System.out.println("--init");
			startTime = Utils.getSystemTime() + WAIT_TIME;
			newState = State.SEARCH_LEFT_WALL;
			System.out.println("--initfin");
			break;
		case SEARCH_LEFT_WALL:
			System.out.println("--searchleftwall");
			HEAD.moveTo(-1000, 1000);
			newState = State.MEASURE_LEFT_WALL;
			System.out.println("--searchleftwallfin");
			break;
		case MEASURE_LEFT_WALL:
			if(!HEAD.isMoving()) {
				leftValue = HEAD.getDistance();
				newState = State.SEARCH_RIGHT_WALL;
			}
			break;
		case SEARCH_RIGHT_WALL:
			HEAD.moveTo(1000, 1000);
			newState = State.MEASURE_LEFT_WALL;
			break;
		case MEASURE_RIGHT_WALL:
			if(!HEAD.isMoving()) {
				rightValue = HEAD.getDistance();
				newState = State.WAIT;
			}
			break;
		case WAIT:
			if (startTime < Utils.getSystemTime()) {
				// on left wall
				if(rightValue > 40) 
					wallFollowerStrategy = WallFollowerStrategy.getRaceStrategy(Side.LEFT);
				else
					wallFollowerStrategy = WallFollowerStrategy.getRaceStrategy(Side.RIGHT);
				wallFollowerStrategy.init();
				newState = State.RACE;
			}
			break;
		case RACE:
			wallFollowerStrategy.run();
			break;
		}
		return newState;
	}
}
