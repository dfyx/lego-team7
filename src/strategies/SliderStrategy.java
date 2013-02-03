package strategies;

import robot.Platform;
import strategies.wall_follower.WallFollowerController;
import utils.Utils;

public class SliderStrategy extends Strategy {
	public enum State {
		FIND_BUTTON,
		PRESS_BUTTON,
		MOVE_TO_SLIDER,
		WAIT_FOR_SLIDER,
		DONE
	}
	protected static final int SLIDER_THRESHOLD_DISTANCE = 20;
	protected static final int MAX_WAIT_TIME = 20 * 1000;

	WallFollowerController wallFollower = new WallFollowerController();

	protected int rightTurnCount = 0;
	protected boolean sliderSlowedDown = false;
	protected boolean sliderReached = false;
	protected int sliderReachedTime = 0;

	@Override
	protected void doInit() {
		rightTurnCount = 0;
		sliderSlowedDown = false;
		sliderReached = false;
		sliderReachedTime = 0;
		wallFollower.init();
	}

	@Override
	protected void doRun() {
		if (rightTurnCount < 2) {
			// Follow left wall for two right turns
			wallFollower.run();
			
			/*
			 * TODO: What I need
			 * WallFollowerController.State oldState = wallFollowerState;
			 * wallFollowerState = wallFollower.getState()
			 * if (wallFollowerState != oldState) {
			 * 	if(oldState == WallFollowerController.TURN_RIGHT) {
			 * 		rightTurnCount++;
			 * 	}
			 * }
			 */
		} else if (!sliderSlowedDown) {
			// Button found, press
			if (Platform.LEFT_BUMPER.getValue() || Platform.RIGHT_BUMPER.getValue()) {
				sliderSlowedDown = true;
				Platform.ENGINE.stop();
			} else {
				Platform.ENGINE.move(-500);
			}
		} else if (!sliderReached){
			wallFollower.run();
			
			/*
			 * TODO: What I need
			 * if (wallFollower.getState() == WallFollowerController.TURN_RIGHT) {
			 * 	// Right before slider, take control
			 * 	Platform.ENGINE.stop();
			 * 	sliderReached = true;
			 *  sliderReachedTime = Utils.getSystemTime();
			 * } 
			 */
		} else if (Platform.HEAD.getDistance() < SLIDER_THRESHOLD_DISTANCE) {
			// Standing right in front of the slider, wait
			if (Utils.getSystemTime() > sliderReachedTime + MAX_WAIT_TIME) {
				// Waited for too long, go back
				sliderSlowedDown = false;
				sliderReached = false;
			}
		} else {
			// Done, drive through the gap
			Platform.ENGINE.move(1000);
		}
	}

}
