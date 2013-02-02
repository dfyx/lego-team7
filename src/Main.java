import lejos.nxt.Button;
import lejos.util.Delay;
import robot.Platform;
import strategies.LineFollowerStrategy;
import strategies.Strategy;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		new Platform();

		while (!Platform.HEAD.isCalibrating())
			Delay.msDelay(1);
		while (Platform.HEAD.isCalibrating())
			Delay.msDelay(500);
		System.out.println("Calibrated");
		System.out.flush();

		while (true) {
			try {
				Strategy test = new LineFollowerStrategy(300);
				Loop loop = new Loop(test);
				loop.start();
				Button.waitForAnyPress();
				loop.abort();
				Platform.ENGINE.stop();
				Platform.ENGINE.commit();
				Button.waitForAnyPress();
				while (loop.isRunning())
					Delay.msDelay(100);
			} finally {
				Platform.ENGINE.stop();
				Platform.ENGINE.commit();
			}
		}
	}
}
