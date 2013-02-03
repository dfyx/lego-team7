package strategies;

import utils.Utils;
import lejos.nxt.Button;

public class StartStrategy extends Strategy {
	public enum State {
		WAIT_FOR_PRESS,
		COUNTDOWN,
		DONE
	}

	private static final int COUNTDOWN_TIME = 10 * 1000;
	
	protected State state;
	protected int countdownEndTime;

	@Override
	protected void doInit() {
		state = State.WAIT_FOR_PRESS;
	}

	@Override
	protected void doRun() {
		state = checkStateTransition();
		
		// Wait and look awesome!
	}

	private State checkStateTransition() {
		switch(state) {
		case WAIT_FOR_PRESS:
			if (Button.ENTER.isDown()) {
				countdownEndTime = Utils.getSystemTime() + COUNTDOWN_TIME;
				return State.COUNTDOWN;
			}
		case COUNTDOWN:
			if (Utils.getSystemTime() > countdownEndTime) {
				setFinished();
				return State.DONE;
			}
		default:
			// WTF?!
			throw new IllegalStateException("Unknown state " + state.toString());
		}
	}

}
