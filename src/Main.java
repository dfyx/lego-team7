import lejos.nxt.Button;
import strategies.wall_follower.WallFollowerController;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		Button.waitForAnyPress();
		
		Loop loop = new Loop();
		WallFollowerController wall = new WallFollowerController();
		loop.run(wall);
	}
}
