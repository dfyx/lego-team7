package strategies.sections;

import static robot.Platform.ENGINE;
import strategies.Strategy;
import strategies.wall_follower.WallFollowerStrategy;
import bluetooth.Gate;

/**
 * A very simple gate strategy. Right now the robot just stops a few
 * centimeters in front of the gate, sends the required bluetooth commands
 * and moves on. If there is enough time left, a wall follower strategy should
 * be used to align the robot.
 * 
 * Experiments showed that there is no need to wait for the gate to open. It's
 * by far fast enough to just drive through.
 */
public class GateStrategy extends Strategy {
	WallFollowerStrategy wallFollower;
	
	public enum State {
		WAIT_FOR_CONNECTION,
		PASS, // pass door parallel to wall
		MOVE // move forward
	}
	
	public GateStrategy() {
		wallFollower = WallFollowerStrategy.getGateStrategy();
	}

	private State currentState;
	
	public State checkStateTransition() {
		switch(currentState) {
		case WAIT_FOR_CONNECTION:
			if (Gate.getInstance().isConnected()) {
				Gate.getInstance().open();
				Gate.getInstance().disconnect();
				return State.PASS;
			}
			break;
		case PASS:
			if(wallFollower.getLostEdgeCount() > 0) {
				ENGINE.move(1000,0);
				return State.MOVE;
			}
			break;
		case MOVE:
			// do nothing and wait for barcode
			break;
		default:
			break;
		}
		
		return currentState;
	}

	@Override
	protected void doInit() {
		currentState = State.WAIT_FOR_CONNECTION;
		Gate.getInstance().connect();
		wallFollower.init();
		ENGINE.stop();
	}

	@Override
	protected void doRun() {
		State oldState = currentState;
		currentState = checkStateTransition();
		if (oldState != currentState) {
			System.out.println(currentState.toString() + " -> " + currentState.toString());
		}
		
		if(currentState == State.PASS) {
			wallFollower.run();
		}
	}

}
