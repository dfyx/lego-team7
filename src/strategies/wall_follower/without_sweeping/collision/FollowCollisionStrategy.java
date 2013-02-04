package strategies.wall_follower.without_sweeping.collision;

import strategies.util.ChildStrategy;
import strategies.wall_follower.without_sweeping.DetectCollisionStrategy;

public class FollowCollisionStrategy extends ChildStrategy {
	DetectCollisionStrategy collisionStrategy;
	
	private enum State {
		START,
		DRIVE,
		COLLIDE,
		STAND,
		DRIVE_BACK,
		TURN_HEAD_FORWARD,
		SEARCH_OBSTACLE, // look forward, if obstacle is there
		AVOID_OBSTACE, // turn left until obstacle goes out of side
		TURN_HEAD_SIDEWAYS,
		SEARCH_WALL
	}

	public FollowCollisionStrategy(int valueCount, int sensitivity) {
		collisionStrategy = new DetectCollisionStrategy(valueCount, sensitivity);
	}

	@Override
	public boolean willStart() {
		return collisionStrategy.willStart();
	}

	@Override
	public boolean isStopped() {
		// TODO SB only used to stay stopped
		return false;
	}

	@Override
	protected void childInit() {
		collisionStrategy.init();
	}

	@Override
	public void check() {
		collisionStrategy.check();
	}

	@Override
	public void work() {
		collisionStrategy.work();
	}

}
