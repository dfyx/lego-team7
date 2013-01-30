import lejos.nxt.Button;
import strategies.FollowWallStrategy;
import strategies.Strategy;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
	    Loop loop = new Loop();
		//Strategy calibrate = new StrategyCalibrateLight();
		//loop.run(calibrate);
	    System.out.println("start");
	    Strategy followWall = new FollowWallStrategy();
	    loop.run(followWall);
		System.out.println("finished");
		Button.waitForAnyPress();
	}
}
