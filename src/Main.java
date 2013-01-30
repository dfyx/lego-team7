import lejos.nxt.Button;
import strategies.Strategy;
import strategies.StrategyCalibrateLight;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
	    Loop loop = new Loop();
		Strategy calibrate = new StrategyCalibrateLight();
		loop.run(calibrate);
		System.out.println("finished");
		Button.waitForAnyPress();
	}
}
