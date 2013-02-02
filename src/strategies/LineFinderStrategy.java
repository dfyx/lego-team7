package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;
import utils.Utils;

/**
 * Tries to find a line, can be used while driving or standing. If a line has
 * been found, the robot will be moved for a hand-over to the LineFinderStrategy.
 * 
 * @author markus
 */
public class LineFinderStrategy extends Strategy {
    
    private static final int DETECTION_THRESHOLD = 500;
    
    private static final double P = 1.0;
    private static final double I = 0;
    private static final double I_DECREASE = 0.5;
    
    private State state = State.NOT_LOCKED;
    
    private int speed = 500;
    
    private int iSum;
    
    @Override
    protected void doInit() {
        state = State.NOT_LOCKED;
        
        iSum = 0;
        // TODO: HEAD-SWEEP +- 75°
    }

    @Override
    protected void doRun() {
        final int value = LIGHT_SENSOR.getValue();
        
        if (value > DETECTION_THRESHOLD) {
            state = State.LINE_FOUND;
            
            ENGINE.stop();
        }
        
        switch(state) {
            case LINE_FOUND:
                // head 10° nach links
                state = State.MOVE_TO_START;
                break;
            case MOVE_TO_START:
                // head steht still? langsamer sweep nach rechts
                state = State.SWEEP_LINE;
                break;
            case SWEEP_LINE:
                if (value > DETECTION_THRESHOLD) {
                    // head anhalten
                    state = State.ALIGN;
                }
                break;
            case ALIGN:
                if (isAligned()) {
                    ENGINE.stop();
                    
                    setFinished();
                } else {
                    alignmentControlLoop(value);
                }
                
                break;
        }
    }
    
    private void alignmentControlLoop(final int value) {
        final int error = DETECTION_THRESHOLD - value;
        
        final int linear = (int) (error * P);
        iSum += error * I;
        
        int out = linear + iSum;
        out = Utils.clamp(out, -1000, 1000);
        
        iSum *= I_DECREASE;
        
        ENGINE.moveCircle(speed, out);
    }
    
    public boolean isAligned() {
        // HEAD gerade?
    }
    
    public boolean isLocked() {
        return state != State.NOT_LOCKED;
    }
    
    private enum State {
        NOT_LOCKED,
        LINE_FOUND,
        MOVE_TO_START,
        SWEEP_LINE,
        ALIGN
    }
}
