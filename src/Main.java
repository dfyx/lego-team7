import java.text.DecimalFormat;

import lejos.nxt.Button;
import lejos.nxt.LCD;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		/*int speed = 0;
		boolean forward = true;
		while(true) {
			System.out.println(speed);
			speed+=25;
			Motor.C.setSpeed(speed);
			if(forward)
				Motor.C.forward();
			else
				Motor.C.backward();
			forward = !forward;
			Button.waitForAnyPress();
		}*/
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
			int lightValue;
			final int ROTATION_MAX_SPEED = 300;
			engine.startRotation(ROTATION_MAX_SPEED);
			do {
				lightValue = sensor.isOnLine();
				LCD.drawString("" + lightValue + "    ", 0, 0);
				engine.startRotation(ROTATION_MAX_SPEED * (100 - lightValue)
						/ 100);
			} while (lightValue < 50);
			engine.stop();
			
			Button.waitForAnyPress();
			
			final int MOVE_SPEED = 300;
			double curvation=2;
			engine.startCurve(MOVE_SPEED,curvation);
			
			while(true) {
				lightValue = sensor.isOnLine();
				if(lightValue>50)
					if(curvation>0.1)
						curvation*=0.999;
				else
					if(curvation<10)
						curvation*=1.001;
				LCD.drawString("Curve: "+(int)1000*curvation, 0,2);
				engine.startCurve(MOVE_SPEED,curvation);
			}
			

			//Button.waitForAnyPress();
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
