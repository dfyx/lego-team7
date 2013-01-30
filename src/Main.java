import lejos.nxt.Button;
import strategies.FollowWallStrategy;
import strategies.Strategy;
import lejos.nxt.comm.RConsole;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		//RConsole.openBluetooth(10);
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
		
		//RConsole.close();
	}
}
