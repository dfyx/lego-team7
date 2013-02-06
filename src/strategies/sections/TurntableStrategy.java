package strategies.sections;

import robot.Platform;
import strategies.line_follower.LineFollowerController;
import strategies.util.StateMachineStrategy;
import bluetooth.TurnControl;
import bluetooth.Turntable;

public class TurntableStrategy extends
		StateMachineStrategy<TurntableStrategy.State> {
	public enum State {
		INIT, FOLLOW_LINE, WAIT_FOR_BOX, DETECT_BOX, TURN, MOVE_BACK, ROTATE_BEFORE_WALL, ROTATE_AFTER_WALL, MOVE_FORWARD, DETECT_EXIT, EXIT_BOX
	}

	public TurntableStrategy() {
		super(State.INIT);
	}

	private static final int BOX_WAIT_DISTANCE = 15;

	// Those will change depending on the robot's geometry
	private static final int BOX_MIN_DISTANCE = 23;
	private static final int BOX_MAX_DISTANCE = 29;

	private static final int LINE_THRESHOLD = 0;

	protected LineFollowerController lineFollower = new LineFollowerController();

	protected TurnControl turntable = new TurnControl();

	protected State run(State currentState) {
		State newState = currentState;
		switch (currentState) {
		case INIT:
			System.out.println("Connect to turntable");
			Turntable.getInstance().connect();
			System.out.println("Switch to follow line");
			lineFollower.init();
			newState = State.FOLLOW_LINE;
			System.out.println("Switched to follow line");
			break;
		case FOLLOW_LINE:
			System.out.println("Run lineFollower");
			lineFollower.run();
			if (lineFollower.isFinished()) {
				Platform.ENGINE.stop();
				Platform.HEAD.moveTo(0, 1000);
				System.out.println("Switch to wait for box");
				newState = State.WAIT_FOR_BOX;
			}
			break;
		case WAIT_FOR_BOX:
			if (Platform.HEAD.getDistance() > BOX_WAIT_DISTANCE
					&& Platform.HEAD.getDistance() != 255
					&& !Platform.HEAD.isMoving()
					&& Turntable.getInstance().isConnected()) {
				System.out.println("Switch to detect box");
				newState = State.DETECT_BOX;
			}
			break;
		case DETECT_BOX:
			if (Platform.HEAD.getDistance() >= BOX_MIN_DISTANCE
					&& Platform.HEAD.getDistance() <= BOX_MAX_DISTANCE) {
				Platform.ENGINE.rotate(-500);
				Platform.HEAD.moveTo(-1000, 1000);
				System.out.println("Switch to turn");
				newState = State.TURN;
			}
			break;
		case TURN:
			if (Platform.HEAD.getLight() > LINE_THRESHOLD) {
				Platform.ENGINE.move(-500);
				Platform.HEAD.moveTo(0, 1000);
				System.out.println("Switch to move back");
				newState = State.MOVE_BACK;
			}
			break;
		case MOVE_BACK:
			if (Platform.LEFT_BUMPER.getValue()
					&& Platform.RIGHT_BUMPER.getValue()) {
				Platform.ENGINE.stop();
				System.out.println("Switch to rotate before wall");
				newState = State.ROTATE_BEFORE_WALL;
			}
			break;
		case ROTATE_BEFORE_WALL:
			if (!Platform.HEAD.isMoving() && Platform.HEAD.getDistance() < 255) {
				Platform.ENGINE.move(500);
				Turntable.getInstance().rotate(90); // Moves 90 to 180 degrees
				System.out.println("Switch to rotate after wall");
				newState = State.ROTATE_AFTER_WALL;
			}
			break;
		case ROTATE_AFTER_WALL:
			if (Platform.HEAD.getLight() > LINE_THRESHOLD) {
				lineFollower.init();
				System.out.println("Switch to exit box");
				newState = State.EXIT_BOX;
			}
			break;
		case EXIT_BOX:
			if (lineFollower.isFinished()) {
				System.out.println("Switch to finished");
				setFinished();
			}
			break;
		default:
			break;
		}

		return newState;
	}
}
