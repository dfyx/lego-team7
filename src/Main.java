import static robot.Platform.ENGINE;
import actors.Engine;
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
			/*
		    Loop loop = new Loop();
			Strategy calibrate = new StrategyCalibrateLight();
			loop.runStrategies(calibrate);
			System.out.println("finished");
			Button.waitForAnyPress();
			*/
			
			SensorArm sensor = new SensorArm();
			
			// Calibrate
			LCD.drawString("Calibrate", 0, 0);
			Button.waitForAnyPress();
			sensor.calibrateLine2(new Engine() {
			    @Override
                public void move(int speed) {
                    super.move(speed);
                    super.commit();
                }
			    @Override
			    public void move(int speed, int direction) {
			        super.move(speed, direction);
			        super.commit();
			    }
			});
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
			final double EXP_FACTOR = 1.0/60000;
			final double LINEAR_FACTOR = 0.6;

			while (true) {
			    int lightValue = sensor.getNormalizedLight();
				int error = lightValue - 500; // positive value => brighter
													// => turn right

				int linear = (int) (LINEAR_FACTOR * error);
				int exponential = (int) (error * error * error * EXP_FACTOR);
				int out = Math.min(1000, Math.max(-1000, linear + exponential));

                LCD.drawString("out: " + out + "    ", 0, 0);
                LCD.drawString("lin: " + linear + "    ", 0, 1);
                LCD.drawString("exp: " + exponential + "    ", 0, 2);
                LCD.drawString("err: " + error + "    ", 0, 3);
                
				ENGINE.move(MOVE_SPEED, out);
				ENGINE.commit();

				Delay.msDelay(10);
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
