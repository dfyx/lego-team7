package strategies.sections;

import robot.Platform;
import strategies.Strategy;
import strategies.line_follower.LineFollowerController;
import strategies.sections.color_finder.ColorScannerStrategy;
import strategies.sections.color_finder.ColorScannerStrategy.ColorName;
import strategies.util.TurnAngleStrategy;
import strategies.wall_follower.WallFollowerStrategy;
import utils.Utils.Side;

public class ColorFinderStrategy extends Strategy {
	public enum State {
		ROTATE_BEFORE_LINE,
		FOLLOW_LINE,
		ROTATE_AFTER_LINE,
		SCAN_COLORS,
		GOTO_COLOR,
		ALIGN_BEFORE_BUTTON,
		PRESS_BUTTON,
		ALIGN_AFTER_BUTTON,
		PASS_GATE
	}

	private static final int WALL_FIND_DISTANCE = 30;
	
	protected State currentState = State.ROTATE_BEFORE_LINE;
	protected TurnAngleStrategy rotateStrategy = new TurnAngleStrategy();
	protected LineFollowerController lineFollower = new LineFollowerController();
	protected ColorScannerStrategy colorScanner = new ColorScannerStrategy(3);

	private ColorName receivedColor;
	
	protected State checkStateTransition() {
		switch(currentState) {
		case ROTATE_BEFORE_LINE:
			if (rotateStrategy.isFinished()) {
				lineFollower.init();
				lineFollower.setState(LineFollowerController.State.OFF_LINE_SEEK);
				Platform.getMainStrategy().disableBarcodeDetection();
				return State.FOLLOW_LINE;
			}
			break;
		case FOLLOW_LINE:
			if (lineFollower.isFinished()
				|| lineFollower.getState() == LineFollowerController.State.OFF_LINE) {
				Platform.HEAD.moveTo(0, 1000);
				Platform.ENGINE.stop();
				rotateStrategy.init();
				rotateStrategy.setSpeed(500);
				rotateStrategy.setTargetAngle(3);
				colorScanner.init();
				return State.ROTATE_AFTER_LINE;
			}
			break;
		case ROTATE_AFTER_LINE:
			if (rotateStrategy.isFinished()) {
				Platform.ENGINE.move(300);
				return State.SCAN_COLORS;
			}
			break;
		case SCAN_COLORS:
			if (colorScanner.isFinished()) {
				Platform.ENGINE.move(-400);
				return State.GOTO_COLOR;
			}
			break;
		case GOTO_COLOR:
			if (Math.abs(Platform.HEAD.getLight()
					- colorScanner.getColor(receivedColor).value)
				< colorScanner.getMinimumDistance() / 2) {
				Platform.ENGINE.stop();
				Platform.HEAD.moveTo(-850, 1000);
				rotateStrategy.init();
				rotateStrategy.setSpeed(500);
				rotateStrategy.setTargetAngle(90);
				return State.ALIGN_BEFORE_BUTTON;
			}
			break;
		case ALIGN_BEFORE_BUTTON:
			if (rotateStrategy.isFinished()
				&& !Platform.HEAD.isMoving()) {
				Platform.HEAD.detectCollisions(true);
				Platform.ENGINE.move(200);
				return State.PRESS_BUTTON;
			}
			break;
		case PRESS_BUTTON:
			if (Platform.HEAD.isColliding()) {
				Platform.ENGINE.move(-200);
				return State.ALIGN_AFTER_BUTTON;
			}
			break;
		}
		return currentState;
	}

	@Override
	protected void doInit() {
		currentState = State.ROTATE_BEFORE_LINE;
		rotateStrategy.init();
		rotateStrategy.setSpeed(500);
		rotateStrategy.setTargetAngle(30);
		
		// Dummy for now
		receivedColor = ColorScannerStrategy.ColorName.RED;
	}

	@Override
	protected void doRun() {
		State oldState = currentState;
		currentState = checkStateTransition();
		if (oldState != currentState) {
			System.out.println("ColorFinderStrategy: "
				+ oldState + " -> " + currentState);
		}
		
		switch(currentState) {
		case ROTATE_BEFORE_LINE:
			rotateStrategy.run();
			break;
		case FOLLOW_LINE:
			lineFollower.run();
			break;
		case ROTATE_AFTER_LINE:
			rotateStrategy.run();
			colorScanner.run();
			break;
		case SCAN_COLORS:
			colorScanner.run();
			break;
		case ALIGN_BEFORE_BUTTON:
			rotateStrategy.run();
			break;
		}
	}

}
