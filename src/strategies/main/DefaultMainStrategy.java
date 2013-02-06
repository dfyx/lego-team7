package strategies.main;

import static robot.Platform.ENGINE;
import lejos.nxt.Button;
import robot.Platform;
import sensors.LightSensor;
import strategies.CountLinesStrategy;
import strategies.LightCalibrationStrategy;
import strategies.Strategy;
import strategies.line_follower.LineFollowerController;
import strategies.sections.BridgeController;
import strategies.sections.GateStrategy;
import strategies.sections.RaceStrategy;
import strategies.sections.SeesawStrategy;
import strategies.sections.SliderStrategy;
import strategies.sections.TurntableStrategy;
import strategies.util.DriveForwardStrategy;
import strategies.wall_follower.WallFollowerStrategy;
import utils.Utils.Side;

public class DefaultMainStrategy extends MainStrategy {

	private boolean detectBarcode;
	private LightSensor.CalibrationData calibration = LightSensor.DEFAULT_CALIBRATION;

	public void disableBarcodeDetection() {
		detectBarcode = false;
	}

	public void enableBarcodeDetection() {
		detectBarcode = true;
	}
	
	public void setClearance() {
		barcodeReader.setClearance(true);
	}

	private enum State {
		WAITING, WAITING_WILL_CALIBRATE, CALIBRATING, WAITING_FOR_STARTSIGNAL, RUNNING
	}

	private enum ButtonState {
		DOWN, UP
	}

	private ButtonState buttonState;

	public static enum Barcode {
		RACE(13), BRIDGE(5), LABYRINTH(7), SWAMP(4), SLIDER(12), GATE(3), SEESAW(10), LINE_TO_PLANT(9), TURNTABLE(11);

		private final int value;

		public static Barcode get(final int value) {
			for (Barcode code : Barcode.values()) {
				if (code.getValue() == value)
					return code;
			}
			throw new IllegalArgumentException("This barcode doesn't exist");
		}

		private Barcode(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	private State state;

	private CountLinesStrategy barcodeReader;

	private Strategy currentStrategy;

	public DefaultMainStrategy() {
	}

	private void switchToCalibrating() {
		ENGINE.stop();
		currentStrategy = new LightCalibrationStrategy();
		currentStrategy.init();
	}

	private void switchToBarcodeReading() {
		ENGINE.stop();
		barcodeReader.init();
		barcodeReader.setClearance(true);
		detectBarcode = true;
		currentStrategy = new DriveForwardStrategy();
		currentStrategy.init();
	}
	
	boolean currentlyMazeLeft = true;

	private void switchLevel(Barcode barcode) {
		ENGINE.stop();
		switch (barcode) {
		case RACE:
			state = State.WAITING_FOR_STARTSIGNAL;
			break;
		case BRIDGE:
		    currentStrategy = new BridgeController();
		    break;
		case SEESAW:
			currentStrategy = new SeesawStrategy();
			break;
		case LINE_TO_PLANT:
		    currentStrategy = new LineFollowerController(); // FIXME, add special strategy
		    break;
		case SWAMP:
			currentStrategy = WallFollowerStrategy.getSwampStrategy();
			break;
		case LABYRINTH:
			// TODO SB switch side
			if(currentlyMazeLeft)
				currentStrategy = WallFollowerStrategy.getMazeStrategy(Side.LEFT);
			else
				currentStrategy = WallFollowerStrategy.getMazeStrategy(Side.RIGHT);
			currentlyMazeLeft = !currentlyMazeLeft;
			break;
		case GATE:
			currentStrategy = new GateStrategy();
			break;
		case SLIDER:
			currentStrategy = new SliderStrategy();
			break;
		case TURNTABLE:
			currentStrategy = new TurntableStrategy();
			break;
		}
		currentStrategy.init();
	}

	@Override
	public void doInit() {
		barcodeReader = new CountLinesStrategy();
		barcodeReader.init();
		state = State.WAITING_WILL_CALIBRATE;
		buttonState = ButtonState.DOWN;
		detectBarcode = true;
		switchToCalibrating();
	}

	/**
	 * Main loop Only run this once!
	 * 
	 * @param strategy
	 *            The strategy to perform
	 * 
	 */
	@Override
	public void doRun() {
		// State change
		if (Button.ENTER.isDown()) {
			if (buttonState == ButtonState.UP) {
				buttonState = ButtonState.DOWN;
			}
		} else if (buttonState == ButtonState.DOWN) {
			buttonState = ButtonState.UP;
			switch (state) {
			case WAITING:
				state = State.RUNNING;
				switchToBarcodeReading();
				break;
			case WAITING_WILL_CALIBRATE:
			case CALIBRATING:
				state = State.CALIBRATING;
				switchToCalibrating();
				break;
			case RUNNING:
				state = State.WAITING;
				ENGINE.stop();
				Platform.HEAD.moveTo(1000,1000);
				break;
			case WAITING_FOR_STARTSIGNAL:
				currentStrategy = new RaceStrategy();
				state = State.RUNNING;
				break;
			}
		}

		// Run child strategy
		if (state == State.RUNNING || state == State.CALIBRATING) {
			// run strategy and commit changes
			barcodeReader.clearStatus();
			if (!Platform.HEAD.isMoving() && detectBarcode)
				barcodeReader.run();
			if (detectBarcode && barcodeReader.hasNewCode()) {
				System.out.println("New barcode: "+barcodeReader.getLineCount());
				int code = barcodeReader.getLineCount();
				if (code > 1)
					switchLevel(Barcode.get(code));
				else
					currentStrategy.run();
			} else {
				currentStrategy.run();
			}
			ENGINE.commit();
		}

		// React, if calibration is finished
		if (state == State.CALIBRATING && currentStrategy.isFinished()) {
		    calibration = Platform.HEAD.getLightSensor().getCalibration();
		    
			state = State.RUNNING;
			switchToBarcodeReading();
		}
		
		//React, if strategy is finished
		if (state == State.RUNNING && currentStrategy.isFinished()) {
			setFinished();
		}
	}

    @Override
    public void restoreLightCalibration() {
        Platform.HEAD.getLightSensor().calibrate(calibration);
    }
}
