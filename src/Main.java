import lejos.util.Delay;
import robot.Platform;

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

		MainStrategy mainStrategy = new MainStrategy();
		Loop loop = new Loop(mainStrategy);
		loop.run();
	}
}
