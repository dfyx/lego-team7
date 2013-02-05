package strategies.sections;

import bluetooth.TurnControl;
import bluetooth.Turntable;
import robot.Platform;
import strategies.LightCalibrationStrategy;
import strategies.line_follower.LineFinderStrategy;
import strategies.line_follower.LineFollowerStrategy;
import strategies.Strategy;

public class TurntableStrategy extends Strategy {
	public enum State {
		CALIBRATE,
		FIND_LINE,		// Will be merged as soon as there is a
		FOLLOW_LINE,	// strategy that does both
		WAIT_FOR_BOX,
		DETECT_BOX,
		TURN,
		MOVE_BACK,
		ROTATE_BEFORE_WALL,
		ROTATE_AFTER_WALL,
		MOVE_FORWARD,
		DETECT_EXIT,
		EXIT_BOX
	}

	private static final int BOX_WAIT_DISTANCE = 15;
	
	// Those will change depending on the robot's geometry
	private static final int BOX_MIN_DISTANCE = 23;
	private static final int BOX_MAX_DISTANCE = 29;

	private static final int LINE_THRESHOLD = 0;
	
	protected State currentState = State.CALIBRATE;
	
	protected LightCalibrationStrategy lightCalibrator = new LightCalibrationStrategy();
	protected LineFinderStrategy lineFinder = new LineFinderStrategy();
	protected LineFollowerStrategy lineFollower = new LineFollowerStrategy();
	
	protected TurnControl turntable = new TurnControl();
	
	protected State checkStateTransition() {
		switch(currentState) {
		case CALIBRATE:
			if (lightCalibrator.isFinished()) {
				lineFinder.init();
				return State.FIND_LINE;
			}
			System.out.println("Finished: " + lightCalibrator.isFinished());
			break;
		case FIND_LINE:
			if (lineFinder.isFinished()) {
				lineFollower.init();
				return State.FOLLOW_LINE;
			}
			break;
		case FOLLOW_LINE:
			if (lineFollower.isFinished()) {
				Platform.ENGINE.stop();
				Platform.HEAD.moveTo(0, 1000);
				return State.WAIT_FOR_BOX;
			}
			break;
		case WAIT_FOR_BOX:
			if (Platform.HEAD.getDistance() > BOX_WAIT_DISTANCE
				&& Platform.HEAD.getDistance() != 255
				&& !Platform.HEAD.isMoving()
				&& Turntable.getInstance().isConnected()) {
				return State.DETECT_BOX;
			}
			break;
		case DETECT_BOX:
			if (Platform.HEAD.getDistance() >= BOX_MIN_DISTANCE
				&& Platform.HEAD.getDistance() <= BOX_MAX_DISTANCE) {
				Platform.ENGINE.rotate(-500);
				Platform.HEAD.moveTo(-1000, 1000);
				return State.TURN;
			}
			break;
		case TURN:
			if (Platform.HEAD.getLight() > LINE_THRESHOLD) {
				Platform.ENGINE.move(-500);
				Platform.HEAD.moveTo(0, 1000);
				return State.MOVE_BACK;
			}
			break;
		case MOVE_BACK:
			if (Platform.LEFT_BUMPER.getValue() && Platform.RIGHT_BUMPER.getValue()) {
				Platform.ENGINE.stop();
				return State.ROTATE_BEFORE_WALL;
			}
			break;
		case ROTATE_BEFORE_WALL:
			if (!Platform.HEAD.isMoving()
				&& Platform.HEAD.getDistance() < 255) {
				Platform.ENGINE.move(500);
				Turntable.getInstance().rotate(90); // Moves 90 to 180 degrees
				return State.ROTATE_AFTER_WALL;
			}
			break;
		case ROTATE_AFTER_WALL:
			if (Platform.HEAD.getLight() > LINE_THRESHOLD) {
				lineFinder.init();
				return State.EXIT_BOX;
			}
			break;
		case EXIT_BOX:
			if (lineFinder.isFinished()) {
				setFinished();
			}
			break;
		default:
			break;
		}
		
		return currentState;
	}
	
	@Override
	protected void doInit() {
		currentState = State.CALIBRATE;
		lightCalibrator.init();
		Turntable.getInstance().connect();
	}

	@Override
	protected void doRun() {
		System.out.print(currentState.toString() + " -> ");
		currentState = checkStateTransition();
		System.out.println(currentState.toString());
		
		switch(currentState) {
		case CALIBRATE:
			lightCalibrator.run();
			break;
		case FIND_LINE:
			lineFinder.run();
			break;
		case FOLLOW_LINE:
			lineFollower.run();
			break;
		case WAIT_FOR_BOX:
			break;
		case DETECT_EXIT:
			break;
		case EXIT_BOX:
			break;
		case MOVE_FORWARD:
			break;
		case ROTATE_AFTER_WALL:
			break;
			
		// States that just waaaaiiiit
		case DETECT_BOX:
		case TURN:
		case MOVE_BACK:
		case ROTATE_BEFORE_WALL:
			break;
		default:
			break;
			
		}
	}
}
