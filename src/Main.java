import lejos.nxt.Button;
import robot.Platform;
import strategies.TestStrategy;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {	
		new Platform();
		
		Button.waitForAnyPress();
		
		Loop loop = new Loop();
		TestStrategy test = new TestStrategy();
		loop.run(test);
	}
}
