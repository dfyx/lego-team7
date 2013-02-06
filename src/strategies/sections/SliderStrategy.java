package strategies.sections;

import robot.Platform;
import strategies.Strategy;
import strategies.line_follower.LineFollowerController;
import strategies.util.DriveForwardStrategy;
import strategies.wall_follower.WallFollowerStrategy;
import utils.Utils;
import utils.Utils.Side;

//TODO Übergang Holz->Schwarz erkennen
public class SliderStrategy extends Strategy {

	private final static int FORWARD_MOVING_TIME = 1000;
	private final static int STOPPING_WALLFOLLOWER_TIME = 5000;
	private final static int MINDISTANCE_TO_SLIDER = 20;
	private final static int MAXDISTANCE_TO_SLIDER = 200;

	public enum State {
		FORWARD, FOLLOW_WALL, STOPPING_WALL_FOLLOWER, POSITION_HEAD_FOR_SLIDER, APPROACH_SLIDER, WAIT_FOR_SLIDER, PASS_SLIDER, FOLLOW_LINE, READ_END_BARCODE
	}

	private State state;
	private int startedDelay;

	private WallFollowerStrategy wallFollowerStrategy = WallFollowerStrategy.getSliderStrategy(Side.RIGHT,10);
	private LineFollowerController lineFollowerController = new LineFollowerController();
	private DriveForwardStrategy driveForwardStrategy = new DriveForwardStrategy();

	@Override
	protected void doInit() {
		state = State.FORWARD;
		Platform.ENGINE.move(1000);
		startedDelay = Utils.getSystemTime();
	}

	@Override
	protected void doRun() {
		switch (state) {
		case FORWARD:
			//System.out.println("Run state forward");
			if (startedDelay + FORWARD_MOVING_TIME < Utils
					.getSystemTime()) {
				wallFollowerStrategy.init();
				state = State.FOLLOW_WALL;
				System.out.println("Switch to follow wall");
			}
			break;
		case FOLLOW_WALL:
			//System.out.println("Run state follow wall");
			wallFollowerStrategy.run();
			if (wallFollowerStrategy.getWallCollisionCount() >= 2) {
				state = State.STOPPING_WALL_FOLLOWER;
				startedDelay = Utils.getSystemTime();
				System.out.println("Swith to stop follow wall");
			}
			break;
		case STOPPING_WALL_FOLLOWER:
			wallFollowerStrategy.run();
			if (startedDelay + STOPPING_WALLFOLLOWER_TIME < Utils
					.getSystemTime()) {
				Platform.HEAD.moveTo(0, 1000);
				state = State.POSITION_HEAD_FOR_SLIDER;
				System.out.println("Switch to position head");
			}
			break;
		case POSITION_HEAD_FOR_SLIDER:
			if (!Platform.HEAD.isMoving()) {
				state = State.APPROACH_SLIDER;
				System.out.println("Switch to approach");
			}
			break;
		case APPROACH_SLIDER:
			if (Platform.HEAD.getDistance() > MAXDISTANCE_TO_SLIDER) {
				//Stop, when slider is open
				Platform.ENGINE.stop();
			} else if (Platform.HEAD.getDistance() > MINDISTANCE_TO_SLIDER) {
				//Move, when slider is closed but far away
				Platform.ENGINE.move(250);
			} else {
				//Stop, when slider is closed and nearby. Then wait for the slider to open
				Platform.ENGINE.stop();
				state = State.WAIT_FOR_SLIDER;
				System.out.println("Switch to wait for slider");
			}
			break;
		case WAIT_FOR_SLIDER:
			if(Platform.HEAD.getDistance()>MAXDISTANCE_TO_SLIDER) {
				Platform.ENGINE.move(1000);
				//Platform.HEAD.startSweeping(-1000,1000,1000);
				wallFollowerStrategy.init();
				state = State.PASS_SLIDER;
				System.out.println("Switch to pass slider");
			}
			break;
		case PASS_SLIDER:
			wallFollowerStrategy.run();
			if(lineFollowerController.lineValueOk()) {
				state = State.FOLLOW_LINE;
				Platform.getMainStrategy().disableBarcodeDetection();
				lineFollowerController.init();
			}
			break;
		case FOLLOW_LINE:
			lineFollowerController.run();
			if(lineFollowerController.isFinished()) {
				state = State.READ_END_BARCODE;
				Platform.getMainStrategy().enableBarcodeDetection();
				Platform.getMainStrategy().setClearance();
				driveForwardStrategy.init();
			}
			break;
		case READ_END_BARCODE:
			driveForwardStrategy.run();
		}
	}
}
