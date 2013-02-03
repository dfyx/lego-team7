package strategies.wall_follower;

import static robot.Platform.HEAD;
import robot.Platform;
import strategies.Strategy;
import utils.Utils.Side;

public class WallFollowerController extends Strategy {

	private enum State {
		AVOID_CRASH, FOLLOW_WALL, FOLLOW_EDGE
	}

	// TODO SB set to find wall?
	private static State currentState = State.FOLLOW_WALL;

	private WallFollowerStrategy wallFollower;
	private QuarterCircleStrategy edgeFollower;
	private AvoidCrashStrategy avoidCrash;
	private static boolean sweeping = true;

	/**
	 * in ms
	 */
	private static long lastNonSweepTime = 0;
	/**
	 * in ms
	 */
	private static final long DONT_SWEEP_TIME = 2000;

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
		if (values.length == 0) {
			System.out.println("no value");
			return 256;
		}
		// TODO SB gibt es ein isSweeping?
		if (!HEAD.isMoving() && !sweeping)
			return HEAD.getDistance();
		// System.out.println("distance: "+values[0]);
		return values[0];
	}

	static void targetWall() {
		if (!sweeping
				&& lastNonSweepTime + DONT_SWEEP_TIME >= System
						.currentTimeMillis())
			return;
		sweeping = true;
		if (headOn == Side.LEFT)
			HEAD.startSweeping(-1000, 0, 2, 2);
		else
			HEAD.startSweeping(1000, 0, 2, 2);
	}

	static void faceForward() {
		HEAD.stopSweeping();
		HEAD.moveTo(0, true);
		sweeping = false;
	}

	static void stopSweeping() {
		HEAD.stopSweeping();
		HEAD.moveTo(-1000, true);
		sweeping = false;
	}

	static Side headOn;

	@Override
	protected void doInit() {
		headOn = Side.LEFT;

		targetWall();

		// TODO SB init in doRun?
		wallFollower = new WallFollowerStrategy();
		edgeFollower = new QuarterCircleStrategy();
		avoidCrash = new AvoidCrashStrategy();
		wallFollower.init();
	}

	@Override
	protected void doRun() {
		if (sweeping && HEAD.getUltrasonicSweepValues().length < 2
				|| HEAD.getUltrasonicSweepValues()[0] > 255
				|| HEAD.getUltrasonicSweepValues()[1] > 255) {
			Platform.ENGINE.stop();
			return;
		}
		System.out.println("sweeping: " + sweeping);
		// TODO SB use curve strategy in this case
		if (currentState != State.AVOID_CRASH && nearlyCrashing()) {
			System.out.println("start avoiding crash");
			currentState = State.AVOID_CRASH;
			avoidCrash.init();
			avoidCrash.run();
			lastNonSweepTime = System.currentTimeMillis();
		} else if (nearlyCrashing()) {
			System.out.println("avoiding crash : "
					+ HEAD.getUltrasonicSweepValues()[1] + " < "
					+ AvoidCrashStrategy.CRASH_DISTANCE);
			currentState = State.AVOID_CRASH;
			avoidCrash.run();
			lastNonSweepTime = System.currentTimeMillis();
		} else if (justAtEnd()) {
			System.out.println("start follow edge");
			sweeping = false;
			currentState = State.FOLLOW_EDGE;
			edgeFollower.init();
			edgeFollower.run();
			lastNonSweepTime = System.currentTimeMillis();
		} else if (atEnd()) {
			System.out.println("follow edge");
			currentState = State.FOLLOW_EDGE;
			edgeFollower.run();
			lastNonSweepTime = System.currentTimeMillis();
		} else {
			System.out.println("follow wall");
			currentState = State.FOLLOW_WALL;
			if (!sweeping)
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

	private boolean nearlyCrashing() {
		if (!sweeping)
			return HEAD.getDistance() < AvoidCrashStrategy.CRASH_DISTANCE;
		if (HEAD.getUltrasonicSweepValues().length < 2)
			return true;
		if (HEAD.getUltrasonicSweepValues()[1] > 255)
			return true;
		return HEAD.getUltrasonicSweepValues()[1] < AvoidCrashStrategy.CRASH_DISTANCE;
	}

}
