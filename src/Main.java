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
	    Platform.HEAD.moveTo(-1000, true);
	    Button.waitForAnyPress();
	    Platform.HEAD.moveTo(0, true);
	    Button.waitForAnyPress();
	    Platform.HEAD.moveTo(1000, true);
	    Button.waitForAnyPress();
	    Platform.HEAD.moveTo(0, true);
	    Button.waitForAnyPress();
	    Platform.HEAD.moveTo(-1000, true);
	    Button.waitForAnyPress();
	    Platform.HEAD.startSweeping(-500, 500, 10);
	    //Strategy followWall = new WallFollowerStrategy();
	    //loop.run(followWall);
	    System.out.println("finished");
	    
		Button.waitForAnyPress();
		
		Loop loop = new Loop();
		WallFollowerController wall = new WallFollowerController();
		loop.run(wall);
	}
}
