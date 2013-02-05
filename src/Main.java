import robot.Platform;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		try {
			new Platform();

			Loop loop = new Loop(Platform.getMainStrategy());
			loop.run();
		} finally {
			Platform.ENGINE.stop();
			Platform.ENGINE.commit();
			Platform.HEAD.terminate();
		}
	}
}
