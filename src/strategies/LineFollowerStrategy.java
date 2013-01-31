package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;

public class LineFollowerStrategy extends Strategy {

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

        ENGINE.move(MOVE_SPEED, out);
    }
}
