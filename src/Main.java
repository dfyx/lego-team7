import lejos.nxt.Button;
import robot.Platform;
import strategies.WallFollowerStrategy;
import strategies.Strategy;
import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 50);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 100);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 150);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 300);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 1500);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
	}
}
