package strategies.sections;

import robot.Platform;
import strategies.Strategy;
import strategies.wall_follower.WallFollowerStrategy;
import static robot.Platform.HEAD;
import static robot.Platform.ENGINE;

public class SeesawStrategy extends Strategy {
	
	WallFollowerStrategy wallFollower = WallFollowerStrategy.getSeesawStrategy();
	BridgeStrategy bridgeStrategy = new BridgeStrategy();

	private enum State {
		POSITIONING_HEAD, // head to front
		WAITING_FOR_BRIDGE_DOWN, // wait for bridge going down
		FOLLOW_BRIDGE, // follow the seesaw
		FOLLOW_WALL, // follow the wall under the bridge
		FIND_LINE, // find line after bridge
		FOLLOW_LINE // follow the line
	}

	private State state;

	// The minimum distance when to trigger "bridge is down"
	final static int MINDISTANCE = 100;
	final static int MINVALUES = 20; // When X values are >MINDISTANCE, the
										// bridge is down.

	int valuecount = 0;

	protected void doInit() {
		state = State.POSITIONING_HEAD;
		Platform.ENGINE.move(-1000);
		Platform.HEAD.moveTo(0, 200);
	}

	protected void doRun() {
		switch (state) {
		case POSITIONING_HEAD:
			if (!Platform.HEAD.isMoving()) {
				Platform.ENGINE.stop();
				state = State.WAITING_FOR_BRIDGE_DOWN;
			}
			break;
		case WAITING_FOR_BRIDGE_DOWN:
			System.out
					.println("wait, Distance: " + Platform.HEAD.getDistance());
			if (Platform.HEAD.getDistance() > MINDISTANCE) {
				++valuecount;
			} else {
				valuecount = 0;
			}
			if (valuecount > MINVALUES) {
				state = State.FOLLOW_BRIDGE;
				bridgeStrategy.init();
			}
			break;
		case FOLLOW_BRIDGE:
			bridgeStrategy.run();
			if(bridgeStrategy.isFinished() && HEAD.getDistance() < 50) {
				wallFollower.init();
				state = State.FOLLOW_WALL;
			}
			break;
		case FOLLOW_WALL:
			wallFollower.run();
			if(wallFollower.getLostEdgeCount() > 1) {
				state = State.FIND_LINE;
				// TODO MJ
				ENGINE.move(1000,0);
			}
			break;
		case FIND_LINE:
			// TODO MJ
			break;
		case FOLLOW_LINE:
			// TODO MJ
			break;
		}
	}
}
