package strategies.wall_follower;

import strategies.Strategy;
import static robot.Platform.HEAD;
import static java.lang.System.out;

public class WallFollowerController extends Strategy {
	private WallFollowerStrategy wallFollower;
	
	// TODO SB marker of -1 is evil. Use boolean flag?
	private int lastDistance = -1;
	/**
	 * No wall in sight
	 */
	private int NO_WALL_DISTANCE = 255;

	@Override
	protected void doInit() {
		// TODO SB init in doRun?
		wallFollower = new WallFollowerStrategy();
		wallFollower.init();
	}

	@Override
	protected void doRun() {
		// TODO SB use curve strategy in this case
		if(lastDistance >= NO_WALL_DISTANCE) {
			out.println("No wall");
		}
		wallFollower.run();
		lastDistance = HEAD.getValue();
	}

}
