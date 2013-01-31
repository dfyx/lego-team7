import lejos.nxt.Button;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import robot.Platform;
import strategies.Strategy;
import strategies.wall_follower.WallFollowerStrategy;
import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;

/**
 * @author markus
 * 
 */

public class Main {

	public static void main(String[] args) {
		NXTRegulatedMotor LEFT_MOTOR = Motor.A;
		NXTRegulatedMotor RIGHT_MOTOR = Motor.B;
		// LEFT_MOTOR.tachoCount();
		Button.waitForAnyPress();

		int lt = LEFT_MOTOR.getTachoCount();
		int rt = RIGHT_MOTOR.getTachoCount();
		ENGINE.moveCircle(500, 50);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		System.out.println("tacho(l/r): " + (LEFT_MOTOR.getTachoCount() - lt)
				+ " / " + +(RIGHT_MOTOR.getTachoCount() - rt));
		lt = LEFT_MOTOR.getTachoCount();
		rt = RIGHT_MOTOR.getTachoCount();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 100);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		System.out.println("tacho(l/r): " + (LEFT_MOTOR.getTachoCount() - lt)
				+ " / " + +(RIGHT_MOTOR.getTachoCount() - rt));
		lt = LEFT_MOTOR.getTachoCount();
		rt = RIGHT_MOTOR.getTachoCount();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 150);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		System.out.println("tacho(l/r): " + (LEFT_MOTOR.getTachoCount() - lt)
				+ " / " + +(RIGHT_MOTOR.getTachoCount() - rt));
		lt = LEFT_MOTOR.getTachoCount();
		rt = RIGHT_MOTOR.getTachoCount();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 300);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		System.out.println("tacho(l/r): " + (LEFT_MOTOR.getTachoCount() - lt)
				+ " / " + +(RIGHT_MOTOR.getTachoCount() - rt));
		lt = LEFT_MOTOR.getTachoCount();
		rt = RIGHT_MOTOR.getTachoCount();
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.moveCircle(500, 1500);
		ENGINE.commit();
		Button.waitForAnyPress();
		ENGINE.stop();
		System.out.println("tacho(l/r): " + (LEFT_MOTOR.getTachoCount() - lt)
				+ " / " + +(RIGHT_MOTOR.getTachoCount() - rt));
		lt = LEFT_MOTOR.getTachoCount();
		rt = RIGHT_MOTOR.getTachoCount();
		ENGINE.commit();
	}
}
