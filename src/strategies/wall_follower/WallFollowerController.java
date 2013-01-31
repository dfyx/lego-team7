package strategies.wall_follower;

import strategies.Strategy;
import static robot.Platform.HEAD;
import static java.lang.System.out;

public class WallFollowerController extends Strategy {

	private WallFollowerStrategy wallFollower;
	private EdgeStrategy driveForward;

	// TODO SB marker of -1 is evil. Use boolean flag?
	private int lastDistance = -1;
	/**
	 * No wall in sight
	 */
	private int NO_WALL_DISTANCE = 255;

	private boolean firstTime = true;

	@Override
	protected void doInit() {
		// TODO SB init in doRun?
		wallFollower = new WallFollowerStrategy();
		driveForward = new EdgeStrategy();
		wallFollower.init();
	}

	@Override
	protected void doRun() {
		// TODO SB use curve strategy in this case
		if (justAtEnd()) {
			out.println("=== NO WALL ===");
			driveForward.init();
			driveForward.run();
		} else if (atEnd()) {
			out.println("===  DRIVE  ===");
			driveForward.run();
		} else {
			wallFollower.run();
			lastDistance = HEAD.getValue();
		}
	}
	
	/**
	 * At end, if last to measurements signaled a wall
	 * @return
	 */
	private boolean atEnd() {
		return lastDistance >= NO_WALL_DISTANCE && HEAD.getValue() >= NO_WALL_DISTANCE;
	}

	private boolean justAtEnd() {
		boolean result = atEnd() && firstTime;
		if(result)
			firstTime = false;
		return result;
	}

}
