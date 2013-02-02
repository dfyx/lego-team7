import lejos.nxt.Button;
import lejos.util.Delay;
import robot.Platform;
import strategies.TestStrategy;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {	
		new Platform();
		
		while(true) {		
			TestStrategy test = new TestStrategy();
			Loop loop = new Loop(test);
			loop.start();
			Button.waitForAnyPress();
			loop.abort();
			Button.waitForAnyPress();
			while(loop.isRunning())
				Delay.msDelay(100);
		}
	}
}
