package strategies.sections;

import strategies.util.StateMachineStrategy;
import utils.Utils;

public class RaceStrategy extends StateMachineStrategy<RaceStrategy.State> {
	
	protected enum State { INIT, WAIT, RACE }
	
	private final static int WAIT_TIME = 10000;
	
	public RaceStrategy() {
		super(State.INIT);
	}
	
	private int startTime;

	@Override
	protected State run(State currentState) {
		State newState = currentState;
		switch(currentState) {
		case INIT:
			startTime = Utils.getSystemTime()+WAIT_TIME;
			newState = State.WAIT;
		case WAIT:
			if(startTime<Utils.getSystemTime()) {
				newState = State.RACE;
			}
		case RACE:
		}
		return newState;
	}
}
