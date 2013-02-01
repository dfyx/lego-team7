package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;
import utils.Utils;

public class LineFollowerStrategy extends Strategy {

    //private static final int CONTROLLER_SLOWDOWN = 5;
    private static final double P = 0.5;
    private static final double D = 5.0;
    private static final double D_DECREASE = 0.5;
    
    //private static final int SAMPLE_COUNT = 5;
    
    //private int sampleCounter = 0;
    //private int sampleValues[] = new int[SAMPLE_COUNT];
    
    //private int slowdownCounter = 0;
    
    private int speed = 500;
    
    private int oldError = 0;
    private int dSum = 0;

    public LineFollowerStrategy() {
        // Nothing to do
    }
    
    public LineFollowerStrategy(final int motorSpeed) {
        if (motorSpeed < 0 || motorSpeed > 1000) {
            throw new IllegalArgumentException("motorSpeed out of range");
        }
        
        this.speed = motorSpeed;
    }
    
    protected void doInit() {
        LIGHT_SENSOR.setFloodlight(true);
    }

    protected void doRun() {
        //sampleValues[sampleCounter] = LIGHT_SENSOR.getValue();
        //sampleCounter = (sampleCounter + 1) % SAMPLE_COUNT;
        
        //slowdownCounter = (slowdownCounter + 1) % CONTROLLER_SLOWDOWN;
        
        //if (slowdownCounter == 0) {  
        //    int meanValue = 0;
            
        //    for (int val : sampleValues) {
        //        meanValue += val;
        //    }
            
        //    meanValue /= SAMPLE_COUNT;
            
        //    final int error = 500 - meanValue;
            final int error = 500 - LIGHT_SENSOR.getValue();
            final int errorD = error - oldError;
            
            final int linear = (int) (P * error);
            dSum += errorD * D;
            final int out = Utils.clamp(linear + dSum, -1000, 1000);
            
            dSum -= out - linear;
            dSum *= D_DECREASE;
            
            System.out.println("err: " + error + " lin: " + linear + " errorD: "
                    + errorD + " dSum: " + dSum + " out: " + out);
    
            ENGINE.move(speed, out);
        //}
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
