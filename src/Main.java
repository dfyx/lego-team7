import lejos.nxt.Button;
import robot.Platform;
import strategies.FollowWallStrategy;
import strategies.Strategy;
import static robot.Platform.ENGINE;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		Platform.HEAD.pauseSweeping();
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
		ENGINE.moveCircle(500, 750);
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
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 750);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		ENGINE.commit();
	}
}
