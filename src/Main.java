import lejos.nxt.Button;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
	    Loop loop = new Loop();
		//Strategy calibrate = new StrategyCalibrateLight();
		loop.loop();//runStrategies(calibrate);
		System.out.println("finished");
		Button.waitForAnyPress();
	}
}
