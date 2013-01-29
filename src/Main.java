import java.text.DecimalFormat;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.util.Delay;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		/*
		 * int speed = 0; boolean forward = true; while(true) {
		 * System.out.println(speed); speed+=25; Motor.C.setSpeed(speed);
		 * if(forward) Motor.C.forward(); else Motor.C.backward(); forward =
		 * !forward; Button.waitForAnyPress(); }
		 */
		try {
			SensorArm sensor = new SensorArm();
			Engine engine = new Engine();

			// Calibrate
			LCD.drawString("Calibrate", 0, 0);
			Button.waitForAnyPress();
			sensor.calibrateLine2(engine);
			LCD.drawString("Calibrated", 0, 0);
			sensor.moveCentral();
			Button.waitForAnyPress();

			// Rotate for line
			/*
			 * int lightValue; final int ROTATION_MAX_SPEED = 300;
			 * engine.startRotation(ROTATION_MAX_SPEED); do { lightValue =
			 * sensor.isOnLine(); LCD.drawString("" + lightValue + "    ", 0,
			 * 0); engine.startRotation(ROTATION_MAX_SPEED * (100 - lightValue)
			 * / 100); } while (lightValue < 50); engine.stop();
			 */

			//Button.waitForAnyPress();

			final int MOVE_SPEED = 400;
			final double ADJUST_FACTOR = 0.5;
			final double DELTA_FACTOR = 4;
			final double DELTA_WEIGHT = 0.1;
			int lastLightValue = sensor.getNormalizedLight();
			int lastDelta = 0;

			while (true) {
				int lightValue = sensor.getNormalizedLight();
				int deltaLight = ((int)((1 - DELTA_WEIGHT)*lastDelta+DELTA_WEIGHT*(lightValue-lastLightValue)));
				int direction = lightValue - 500; // positive value => brighter
													// => turn right
				direction *= ADJUST_FACTOR;
				LCD.drawString("Abs: "+direction+"    ",0,1);
				LCD.drawString("Diff: "+(deltaLight*DELTA_FACTOR)+"    ",0,2);
				direction += deltaLight * DELTA_FACTOR; 
				direction = Math.min(1000, Math.max(-1000, direction));
				engine.startCurve(MOVE_SPEED, direction);
				LCD.drawString("Curve: " + direction + "    ", 0, 0);
				Delay.msDelay(10);
				lastLightValue = lightValue;
				lastDelta = deltaLight;
			}

			// Button.waitForAnyPress();
		} catch (Exception e) {
			System.out.println("Exception: " + e.getMessage());
			Button.waitForAnyPress();
		}

		/*
		 * System.out.println("Wait for connection"); NXTConnection connection =
		 * Bluetooth.waitForConnection(); System.out.println("connected");
		 * DataInputStream dataIn = connection.openDataInputStream();
		 * System.out.println("Wait for data"); try { while(true) { int
		 * value=dataIn.readInt(); System.out.println("Read");
		 * System.out.flush(); System.out.println(""+value); System.out.flush();
		 * } } catch (IOException e ) {
		 * System.out.println("read error "+e.getMessage()); }
		 * Button.waitForAnyPress();
		 */
	}
}
