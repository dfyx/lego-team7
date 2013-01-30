package strategies;

<<<<<<< HEAD
public class StrategyFollowLine extends Strategy {

	@Override
	protected void doInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doRun() {
		// TODO Auto-generated method stub
		
	}

}
=======
import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;

public class StrategyFollowLine extends Strategy {

    private static final int MOVE_SPEED = 400;
    private static final double EXP_FACTOR = 1.0 / 60000;
    private static final double LINEAR_FACTOR = 0.6;

    protected void doInit() {

    }

    protected void doRun() {
        final int error = 500 - LIGHT_SENSOR.getValue();

        final int linear = (int) (LINEAR_FACTOR * error);
        final int exponential = (int) (error * error * error * EXP_FACTOR);
        final int out = Math.min(1000, Math.max(-1000, linear + exponential));

        /*
         * LCD.drawString("out: " + out + "    ", 0, 0);
         * LCD.drawString("lin: " + linear + "    ", 0, 1);
         * LCD.drawString("exp: " + exponential + "    ", 0, 2);
         * LCD.drawString("err: " + error + "    ", 0, 3);
         */

        ENGINE.move(MOVE_SPEED, out);
    }
}
>>>>>>> master
