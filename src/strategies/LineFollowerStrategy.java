package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import utils.Utils;
import utils.Utils.Side;

public class LineFollowerStrategy extends Strategy {

    private static final int LIGHT_SETPOINT = 550; // FIXME: Adjust at monday
    
    private static final int LINE_LOSS_LIMIT = 5;
    private static final int LINE_LOSS_THRESHOLD = 100; // FIXME: Adjust at monday
    
    private static final double P = 0.8;
    private static final double D = 2.0;
    
    private int speed = 500;
    private int clamp = 1000;
    private Side trackingSide = Side.LEFT;
    
    private boolean onLine;
    private int lineLossCounter;
    
    private int oldError;
    private int dSum;
    
    private int lastSpeed;
    private int lastDirection;

    public LineFollowerStrategy() {
        doInit();
    }
    
    protected void doInit() {
        onLine = true;
        lineLossCounter = 0;
        
        oldError = 0;
        dSum = 0;
        
        lastSpeed = 0;
        lastDirection = 0;
        
        HEAD.moveTo(0, false);
    }

    protected void doRun() {
        final int value = HEAD.getLight();
        
        if (onLine && value < LINE_LOSS_THRESHOLD) {
            onLine = ++lineLossCounter <= LINE_LOSS_LIMIT;
        } else if (value > LINE_LOSS_THRESHOLD) {
            lineLossCounter = 0;
        }

        if (onLine) {
            controlLoop(value);
        } else {
            if (lineLossCounter > 0) {
                lineLossCounter--;
                
                // Try to undo the last LINE_LOSS_LIMIT movements, assuming a
                // constant (maximum) controller output value
                ENGINE.move(-lastSpeed, lastDirection);
            } else {
                ENGINE.stop();
                
                setFinished(); // line lost, stop work
            }
        }
    }

    private void controlLoop(final int value) {
        final int error = trackingSide.getValue() * (value - LIGHT_SETPOINT);
        final int errorD = error - oldError;

        oldError = error;

        final int linear = (int) (P * error);
        dSum += errorD * D;
        final int out = Utils.clamp(linear + dSum, -clamp, clamp);

        dSum -= out - linear;

        /*
        System.out.println("val: " + value + " err: " + error + " lin: "
                + linear + " errorD: " + errorD + " dSum: " + dSum + " out: "
                + out);
         */

        lastSpeed = speed;
        lastDirection = out;

        ENGINE.move(lastSpeed, lastDirection);
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public void setSpeed(final int motorSpeed) {
        if (motorSpeed < 0 || motorSpeed > 1000) {
            throw new IllegalArgumentException("motorSpeed out of range");
        }
        
        speed = motorSpeed;
    }
    
    public int getClamp() {
        return clamp;
    }
    
    public void setClamp(final int clamp) {
        if (clamp < 0 || clamp > 1000) {
            throw new IllegalArgumentException("motorSpeed out of range");
        }
        
        this.clamp = clamp;
    }
    
    public Side getTrackingSide() {
        return trackingSide;
    }
    
    public void setTrackingSide(final Side side) {
        trackingSide = side;
    }
}
