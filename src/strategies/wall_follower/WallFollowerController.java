package strategies.wall_follower;

import static robot.Platform.HEAD;
import strategies.Strategy;

public class WallFollowerController extends Strategy {

	private WallFollowerStrategy wallFollower;
	private QuarterCircleStrategy edgeFollower;

	// TODO SB marker of -1 is evil. Use boolean flag?
	private int lastDistance = -1;
	/**
	 * No wall in sight
	 */
	private int NO_WALL_DISTANCE = 50;

	private boolean firstTime = true;

	@Override
	protected void doInit() {
		// TODO SB init in doRun?
		wallFollower = new WallFollowerStrategy();
		edgeFollower = new QuarterCircleStrategy();
		wallFollower.init();
	}

	@Override
	protected void doRun() {
		// TODO SB use curve strategy in this case
		if (justAtEnd()) {
			System.out.println("-  just -");
			edgeFollower.init();
			edgeFollower.run();
		} else if (atEnd()) {
			System.out.println("-  end  -");
			edgeFollower.run();
		} else {
			System.out.println("- follow -");
			firstTime = true;
			wallFollower.run();
		}
		lastDistance = HEAD.getDistance();
	}

	/**
	 * At end, if last to measurements signaled a wall
	 * 
	 * @return
	 */
	private boolean atEnd() {
		return lastDistance >= NO_WALL_DISTANCE
				&& HEAD.getDistance() >= NO_WALL_DISTANCE;
	}

	private boolean justAtEnd() {
		boolean result = atEnd() && firstTime;
		if (result)
			firstTime = false;
		return result;
	}

}
