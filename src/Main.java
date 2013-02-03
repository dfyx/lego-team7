import lejos.nxt.Button;
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

		while (true) {
			try {
				WallFollowerWithoutCollisionController wall = new WallFollowerWithoutCollisionController(Side.RIGHT);
				Loop loop = new Loop(wall);
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
