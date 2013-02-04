import lejos.util.Delay;
import robot.Platform;
import strategies.Strategy;
import strategies.TestStrategy;

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

		Strategy mainStrategy = new Strategy() {

			@Override
			protected void doInit() {
				Platform.HEAD.moveTo(800, false);
				Delay.msDelay(1000);
				Platform.HEAD.detectCollisions(true);
				Delay.msDelay(1000);
			}

			@Override
			protected void doRun() {
				System.out.println(Platform.HEAD.isColliding());
			}
			
		};
		Loop loop = new Loop(mainStrategy);
		loop.run();
	}
}
