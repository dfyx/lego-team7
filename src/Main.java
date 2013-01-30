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

		//Loop loop = new Loop();
		//Strategy calibrate = new StrategyCalibrateLight();
		//loop.run(calibrate);
		//System.out.println("finished");
	    Loop loop = new Loop();
		//Strategy calibrate = new StrategyCalibrateLight();
		//loop.run(calibrate);
	    //RConsole.println("start");
	    System.out.println("start");
	    Strategy followWall = new FollowWallStrategy();
	    loop.run(followWall);
	    System.out.println("finished");
	    //RConsole.println("finished");
		Button.waitForAnyPress();
	}
}
