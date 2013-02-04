import static robot.Platform.ENGINE;
import lejos.nxt.Button;
import robot.Platform;
import strategies.CountLinesStrategy;
import strategies.DriveForwardStrategy;
import strategies.LightCalibrationStrategy;
import strategies.Strategy;
import strategies.sections.RaceStrategy;
import strategies.wall_follower.WallFollowerStrategy;

public class MainStrategy extends Strategy {
	
	private boolean detectBarcode;
	
	public void disableBarcodeDetection() {
		detectBarcode=false;
	}
	
	public void enableBarcodeDetection() {
		detectBarcode = true;
	}

	private enum State {
		WAITING, CALIBRATING, WAITING_FOR_STARTSIGNAL, RUNNING
	}

	private enum ButtonState {
		DOWN, UP
	}

	private ButtonState buttonState;

	public static enum Barcode {
		RACE(1), LABYRINTH(5);

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

	public MainStrategy() {
	}

	private void switchToCalibrating() {
		ENGINE.stop();
		currentStrategy = new LightCalibrationStrategy();
		currentStrategy.init();
	}

	private void switchToBarcodeReading() {
		ENGINE.stop();
		currentStrategy = new DriveForwardStrategy();
		currentStrategy.init();
	}

	private void switchLevel(Barcode barcode) {
		ENGINE.stop();
		switch (barcode) {
		case RACE:
			state = State.WAITING_FOR_STARTSIGNAL;
			System.out.println("->WAIT FOR RACE");
			break;
		case LABYRINTH:
			System.out.println("->LABYRINTH");
			currentStrategy = new WallFollowerStrategy();
			break;
		}
		currentStrategy.init();
	}

	@Override
	public void doInit() {
		barcodeReader = new CountLinesStrategy();
		barcodeReader.init();
		state = State.WAITING;
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
			case CALIBRATING:
				state = State.CALIBRATING;
				System.out.println("->CALIBRATING");
				switchToCalibrating();
				break;
			case RUNNING:
				state = State.WAITING;
				System.out.println("->WAITING");
				break;
			case WAITING_FOR_STARTSIGNAL:
				currentStrategy = new RaceStrategy();
				System.out.println("->START RACE");
				state = State.RUNNING;
				break;
			}
		}

		//Run child strategy
		if (state == State.RUNNING || state == State.CALIBRATING) {
			// run strategy and commit changes
			if (!Platform.HEAD.isMoving())
				barcodeReader.run();
			if (detectBarcode && barcodeReader.hasNewCode()) {
				switchLevel(Barcode.get(barcodeReader.getLineCount()));
			} else {
				currentStrategy.run();
			}
			ENGINE.commit();
		}

		//React, if calibration is finished
		if (state == State.CALIBRATING && currentStrategy.isFinished()) {
			state = State.RUNNING;
			System.out.println("->RUNNING (barcode)");
			switchToBarcodeReading();
		}
	}
}
