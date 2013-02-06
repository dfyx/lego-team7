package strategies.line_follower;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import strategies.Strategy;
import utils.RunningAverage;
import utils.Utils;
import utils.Utils.Side;

public class LineFollowerStrategy extends Strategy {

    public static final Parameters LINE_PARAMS = new LineParameters();
    public static final Parameters BRIDGE_PARAMS = new BridgeParameters();
    
    protected static final int LIGHT_SETPOINT = LINE_PARAMS.lightSetpoint();
    
    private Parameters params = LINE_PARAMS;
    
    private int speed = 500;
    private int clamp = 1000;
    private Side trackingSide = Side.LEFT;
    
    private boolean onLine;
    private int lineLossCounter;
    
    private int oldError;
    private int dSum;
    
    private RunningAverage lastSpeeds;
    private RunningAverage lastDirections;

    public LineFollowerStrategy() {
        doInit();
    }
    
    public LineFollowerStrategy(final Parameters params) {
        this();
        this.params = params;
    }
    
    protected void doInit() {
        onLine = true;
        lineLossCounter = 0;
        
        oldError = 0;
        dSum = 0;
        
        lastSpeeds = new RunningAverage(params.lossLimit());
        lastDirections = new RunningAverage(params.lossLimit());
    }

    protected void doRun() {
        final int value = HEAD.getLight();
        
        if (onLine && value < params.lossThreshold()) {
            onLine = ++lineLossCounter <= params.lossLimit();
            
            if (!onLine) {
                lineLossCounter = params.lossUndo();
            }
        } else if (value > params.lossThreshold()) {
            lineLossCounter = 0;
        }

        if (onLine) {
            controlLoop(value);
        } else {
            if (lineLossCounter > 0) {
                lineLossCounter--;
                
                // Try to undo the last LINE_LOSS_LIMIT movements, assuming a
                // constant (maximum) controller output value
                ENGINE.move(-lastSpeeds.getAverage(), lastDirections.getAverage());
            } else {
                ENGINE.stop();
                
                setFinished(); // line lost, stop work
            }
        }
    }

    private void controlLoop(final int value) {
        final int error = trackingSide.getValue() * (value - params.lightSetpoint());
        final int errorD = error - oldError;

        oldError = error;

        final int linear = (int) (params.p() * error);
        dSum += errorD * params.d();
        final int out = Utils.clamp(linear + dSum, -clamp, clamp);

        dSum -= out - linear;

        /*
        System.out.println("val: " + value + " err: " + error + " lin: "
                + linear + " errorD: " + errorD + " dSum: " + dSum + " out: "
                + out);
        */

        lastSpeeds.addValue(speed);
        lastDirections.addValue(out);

        ENGINE.move(speed, out);
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
    
    interface Parameters {
        int lightSetpoint();
        int lossThreshold();
        int lossLimit();
        int lossUndo();
        float p();
        float d();
    }
    
    private static class LineParameters implements Parameters {
        @Override
        public int lightSetpoint() {
            return 600;
        }

        @Override
        public int lossThreshold() {
            return 250;
        }

        @Override
        public float p() {
            return (float) 0.75;
        }

        @Override
        public float d() {
            return (float) 2.5;
        }

        @Override
        public int lossLimit() {
            return 8;
        }

        @Override
        public int lossUndo() {
            return 20;
        }
    }
    
    private static class BridgeParameters implements Parameters {
        @Override
        public int lightSetpoint() {
            return 350;
        }

        @Override
        public int lossThreshold() {
            return 75;
        }

        @Override
        public float p() {
            return (float) 2.0;
        }

        @Override
        public float d() {
            return (float) 4.0;
        }

        @Override
        public int lossLimit() {
            return 15;
        }

        @Override
        public int lossUndo() {
            return 30;
        }
    }
}
