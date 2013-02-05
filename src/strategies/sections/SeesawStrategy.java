package strategies.sections;

import robot.Platform;
import strategies.Strategy;

public class SeesawStrategy extends Strategy {
	
	private enum State { POSITIONING_HEAD, WAITING_FOR_BRIDGE_DOWN, MOVING }
	private State state;
	
	//The minimum distance when to trigger "bridge is down"
	final static int MINDISTANCE=100;
	final static int MINVALUES = 20; //When X values are >MINDISTANCE, the bridge is down.
	
	int valuecount=0;

	protected void doInit() {
		state = State.POSITIONING_HEAD;
		Platform.ENGINE.move(-1000);
		Platform.HEAD.moveTo(0, 200);
	}

	protected void doRun() {
		switch(state) {
		case POSITIONING_HEAD:
			if(!Platform.HEAD.isMoving()) {
				Platform.ENGINE.stop();
				state = State.WAITING_FOR_BRIDGE_DOWN;
			}
			break;
		case WAITING_FOR_BRIDGE_DOWN:
			System.out.println("wait, Distance: "+Platform.HEAD.getDistance());
			if(Platform.HEAD.getDistance()>MINDISTANCE) {
				++valuecount;
			} else {
				valuecount=0;
			}
			if(valuecount>MINVALUES) {
				state = State.MOVING;
				Platform.ENGINE.move(1000);
				Platform.HEAD.startSweeping(-1000, 1000, 1000);
			}
			break;
		case MOVING:
			if(Platform.HEAD.getLight()>500) {
				Platform.HEAD.stopSweeping();
				setFinished();
			}
		}
	}
}
