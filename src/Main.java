import lejos.nxt.Button;
import robot.Platform;
import strategies.FollowWallStrategy;
import strategies.Strategy;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		new Platform();

	    Loop loop = new Loop();
	    
	    System.out.println("start");
	    Strategy followWall = new FollowWallStrategy();
	    loop.run(followWall);
	    System.out.println("finished");
	    
		Button.waitForAnyPress();
	}
}
