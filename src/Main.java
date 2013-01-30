import static robot.Platform.ENGINE;
import legacy.SensorArm;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.util.Delay;
import robot.Platform;
import actors.Engine;
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
		Platform.HEAD.pauseSweeping();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 92);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 184);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 450);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 600);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 750);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
	    /*Loop loop = new Loop();
		//Strategy calibrate = new StrategyCalibrateLight();
		//loop.run(calibrate);
	    //RConsole.println("start");
	    System.out.println("start");
	    Strategy followWall = new FollowWallStrategy();
	    loop.run(followWall);
	    System.out.println("finished");
	    //RConsole.println("finished");
		Button.waitForAnyPress();*/
	}
}
