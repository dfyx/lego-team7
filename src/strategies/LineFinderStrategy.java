package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import utils.Utils;

/**
 * Tries to find a line, can be used while driving or standing. If a line has
 * been found, the robot will be moved for a hand-over to the LineFinderStrategy.
 * 
 * @author markus
 */
public class LineFinderStrategy extends Strategy {
    
    /** Brightness treshold used for line detection. */
    private static final int DETECTION_THRESHOLD = 500;
    /** Sweep angle used during initial line search. */
    private static final int DETECTION_ANGLE = 45;
    /** Sensor head movement angle used to position the light sensor at the 
     * required side of the line (left). */
    private static final int SEARCH_OFFSET_ANGLE = -10;
    /** Alignment tolerance used to end the alignment process. */
    private static final int ALIGNMENT_TOLERANCE_ANGLE = 5;
    
    private static final double P = 1.0;
    private static final double I = 0.05;
    private static final double I_DECREASE = 0.5;
    
    private State state = State.NOT_LOCKED;
    
    private int driveSpeed = 500;
    private int sweepSpeed = 400;
    
    private int iSum;
    
     // FIXME: calculation in head
    private final int degreeToHead(final int degrees) {
        return (degrees * 1000) / 90;
    }
    
    // FIXME: calculation in head
    private final int headToDegrees(final int head) {
        return (head * 90) / 1000;
    }
    
    @Override
    protected void doInit() {
        state = State.NOT_LOCKED;
        
        iSum = 0;
        
        // Sweep wildly until a line has been found
        final int range = degreeToHead(DETECTION_ANGLE);
        HEAD.setSweepSpeed(sweepSpeed);
        HEAD.startSweeping(-range, range, 2, 2); 
    }

    @Override
    protected void doRun() {
        final int value = HEAD.getLight();
        
        switch(state) {
            case NOT_LOCKED:
                if (value > DETECTION_THRESHOLD) {
                    // line found, stop engines and align
                    state = State.LINE_FOUND;
                    
                    System.out.println("Line found");
                    
                    ENGINE.stop();
                }
                break;
            case LINE_FOUND:
                // move head to a position left of the line
                HEAD.stopSweeping();
                HEAD.moveTo(degreeToHead(headToDegrees(
                        HEAD.getPosition() + SEARCH_OFFSET_ANGLE)), true);
                
                System.out.println("Stopped sweeping");
                
                state = State.MOVE_TO_START;
                break;
            case MOVE_TO_START:
                if (!HEAD.isMoving()) {
                    // head arrived at the left hand side of the line
                    // start a slow sweep to the right
                    HEAD.moveTo(degreeToHead(headToDegrees(HEAD.getPosition()
                            - 2 * SEARCH_OFFSET_ANGLE)), true, sweepSpeed);
                    
                    state = State.SWEEP_LINE;
                    
                    System.out.println("Searching line again");
                }
                break;
            case SWEEP_LINE:
                if (value > DETECTION_THRESHOLD) {
                    // head hit the line again, start alignment
                    state = State.ALIGN;
                    
                    System.out.println("Aligning");
                }
                break;
            case ALIGN:
                if (isAligned()) {
                    ENGINE.stop();
                    HEAD.stopMoving();
                    
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
        out = Utils.clamp(out, -driveSpeed, driveSpeed); // limit to drive speed
        
        iSum *= I_DECREASE;
        
        ENGINE.move(out, 1000); // TODO: Test, but should work
    }
    
    /**
     * Checks if a line has been found.
     * 
     * @return true if a line has been found
     */
    public boolean isLocked() {
        return state != State.NOT_LOCKED;
    }
    
    /**
     * Checks if the alignment process has been completed.
     * 
     * @return true if a line has been found and the alignment is done
     */
    public boolean isAligned() {
        return isLocked()
                && headToDegrees(HEAD.getPosition()) < ALIGNMENT_TOLERANCE_ANGLE;
    }
    
    /**
     * Gets the driving speed used for alignment.
     * 
     * @return a speed value suitable for {@link actors.Engine}
     */
    public int getDriveSpeed() {
        return driveSpeed;
    }
    
    /**
     * Sets the driving speed used for alignment.
     * 
     * @param driveSpeed
     *            the speed value, required to be within the value range
     *            accepted by {@link actors.Engine}
     */
    public void setDriveSpeed(int driveSpeed) {
        this.driveSpeed = driveSpeed;
    }
    
    /**
     * Gets the head sweep speed used for alignment.
     * 
     * @return a speed value suitable for {@ sensors.Head}
     */
    public int getSweepSpeed() {
        return sweepSpeed;
    }
    
    /**
     * Sets the head sweep speed used for alignment.
     * 
     * @param sweepSpeed
     *            the speed value, required to be within the value range
     *            accepted by {@link sensors.HEAD}
     */
    public void setSweepSpeed(int sweepSpeed) {
        this.sweepSpeed = sweepSpeed;
    }
    
    private enum State {
        NOT_LOCKED,
        LINE_FOUND,
        MOVE_TO_START,
        SWEEP_LINE,
        ALIGN
    }
}
