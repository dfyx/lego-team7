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

		Strategy mainStrategy = new TestStrategy();
		Loop loop = new Loop(mainStrategy);
		loop.run();
	}
}
