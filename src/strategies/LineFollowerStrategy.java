package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;
import utils.Utils;

public class LineFollowerStrategy extends Strategy {

    private static final int LIGHT_SETPOINT = 500;
    
    private static final int LINE_LOSS_LIMIT = 5;
    private static final int LINE_LOSS_THRESHOLD = 50;
    
    private static final double P = 0.5;
    private static final double D = 5.0;
    private static final double D_DECREASE = 0.75;
    
    private int speed = 500;
    
    private boolean onLine;
    private int lineLossCounter;
    
    private int oldError;
    private int dSum;
    
    private int lastSpeed;
    private int lastDirection;

    public LineFollowerStrategy() {
        doInit();
    }
    
    public LineFollowerStrategy(final int motorSpeed) {
        this();
        
        if (motorSpeed < 0 || motorSpeed > 1000) {
            throw new IllegalArgumentException("motorSpeed out of range");
        }        
        
        this.speed = motorSpeed;
    }
    
    protected void doInit() {
        onLine = true;
        lineLossCounter = 0;
        
        oldError = 0;
        dSum = 0;
        
        lastSpeed = 0;
        lastDirection = 0;
    }

    protected void doRun() {
        final int value = LIGHT_SENSOR.getValue();
        
        if (onLine && value < LINE_LOSS_THRESHOLD) {
            onLine = ++lineLossCounter <= LINE_LOSS_LIMIT;
        }
        
        if (onLine) {
            controlLoop(value);
        } else {
            if (lineLossCounter > 0) {
                lineLossCounter--;
                
                // Try to undo the last LINE_LOSS_LIMIT movements, assuming a
                // constant (maximum) controller output value
                ENGINE.move(lastSpeed, -lastDirection);
            } else {
                ENGINE.stop();
                
                setFinished(); // line lost, stop work
            }
        }
    }
    
    private void controlLoop(final int value) {
            final int error = LIGHT_SETPOINT - value;
            final int errorD = error - oldError;
            
            final int linear = (int) (P * error);
            dSum += errorD * D;
            final int out = Utils.clamp(linear + dSum, -1000, 1000);
            
            dSum -= out - linear;
            dSum *= D_DECREASE;
            
            System.out.println("err: " + error + " lin: " + linear + " errorD: "
                    + errorD + " dSum: " + dSum + " out: " + out);
    
            lastSpeed = speed;
            lastDirection = out;
            
            ENGINE.move(lastSpeed, lastDirection);
    }
    
    public int getSpeed() {
        return speed;
    }
    
    public void setSpeed(int motorSpeed) {
        if (motorSpeed < 0 || motorSpeed > 1000) {
            throw new IllegalArgumentException("motorSpeed out of range");
        }
        
        speed = motorSpeed;
    }
}
