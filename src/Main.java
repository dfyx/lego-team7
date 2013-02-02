import lejos.nxt.Button;
import robot.Platform;
import strategies.wall_follower.WallFollowerController;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {	
		new Platform();
	    System.out.println("start");
		Button.waitForAnyPress();
		
		Loop loop = new Loop();
		WallFollowerController wall = new WallFollowerController();
		loop.run(wall);
		
	    System.out.println("finished");
	}
}
