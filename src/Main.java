import lejos.util.Delay;
import robot.Platform;
import strategies.wall_follower.without_sweeping.WallFollowerWithoutCollisionController;
import utils.Utils.Side;

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

		WallFollowerWithoutCollisionController mainStrategy = new WallFollowerWithoutCollisionController(
				Side.RIGHT);
		Loop loop = new Loop(mainStrategy);
		loop.run();
	}
}
