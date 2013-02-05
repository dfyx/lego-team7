import robot.Platform;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		new Platform();

		Loop loop = new Loop(Platform.getMainStrategy());
		loop.run();
	}
}
