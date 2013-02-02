package strategies.wall_follower;

import static robot.Platform.HEAD;
import lejos.nxt.Motor;
import strategies.Strategy;

public class WallFollowerController extends Strategy {

	private WallFollowerStrategy wallFollower;
	private QuarterCircleStrategy edgeFollower;
	private static boolean sweeping = true;
	
	/**
	 * in ms
	 */
	private static long lastNonSweepTime = 0;
	/**
	 * in ms
	 */
	private static final long DONT_SWEEP_TIME = 1000000;

	// TODO SB marker of -1 is evil. Use boolean flag?
	static int lastDistance = -1;
	/**
	 * No wall in sight
	 */
	private int NO_WALL_DISTANCE = 50;

	private boolean firstTime = true;
	
	static int getWallDistance() {
		int[] values = HEAD.getUltrasonicSweepValues();
		
		// TODO SM (SB) should never happen. Fix this in 'HEAD'
		// No value found
		if(values.length == 0) {
			System.out.println("no value");
			return 256;
		}
		// TODO SB gibt es ein isSweeping?
		if(!HEAD.isMoving() && !sweeping)
			return HEAD.getDistance();
		//System.out.println("distance: "+values[0]);
		return values[0];
	}
	
	static void targetWall() {
		// TODO SB async
//		if(headOn == HeadOn.LEFT_SIDE)
//			HEAD.moveTo(-1000, false);
//		else
//			HEAD.moveTo(1000, false);
		if(!sweeping && lastNonSweepTime + DONT_SWEEP_TIME < System.currentTimeMillis()) {
			System.out.println("not sweeping");
			return;
		} else 
			System.out.println("sweeping");
		sweeping = true;
		if(headOn == HeadOn.LEFT_SIDE)
			HEAD.startSweeping(-1000, 0, 2, 2);
		else
			HEAD.startSweeping(1000, 0, 2, 2);
	}
	
	static void stopSweeping() {
		HEAD.stopSweeping();
		HEAD.moveTo(-1000, true);
		sweeping = false;
	}
	
	/**
	 * 
	 * LEFT_SIDE in driving direction
	 *
	 */
	enum HeadOn {
		RIGHT_SIDE, LEFT_SIDE
	}
	
	static HeadOn headOn;

	@Override
	protected void doInit() {
		headOn = HeadOn.LEFT_SIDE;
		
		targetWall();
		
		// TODO SB init in doRun?
		wallFollower = new WallFollowerStrategy();
		edgeFollower = new QuarterCircleStrategy();
		wallFollower.init();
	}

	@Override
	protected void doRun() {
		// TODO SB use curve strategy in this case
		if (justAtEnd()) {
			edgeFollower.init();
			edgeFollower.run();
			lastNonSweepTime = System.currentTimeMillis();
		} else if (atEnd()) {
			edgeFollower.run();
			lastNonSweepTime = System.currentTimeMillis();
		} else {
			if(!sweeping)
				targetWall();
			firstTime = true;
			wallFollower.run();
		}
		lastDistance = getWallDistance();
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
