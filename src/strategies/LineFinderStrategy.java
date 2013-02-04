package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import sensors.Head;
import utils.Utils;
import utils.Utils.Side;

/**
 * Tries to find a line, can be used while driving or standing. If a line has
 * been found, the robot will be moved for a hand-over to the LineFinderStrategy.
 * 
 * @author markus
 */
public class LineFinderStrategy extends Strategy {
    
    /** Brightness treshold used for line detection. */
    private static final int DETECTION_THRESHOLD = LineFollowerStrategy.LIGHT_SETPOINT + 50;
    /** Sweep angle used during initial line search. */
    private static final int DETECTION_ANGLE = 75;
    /** Maximum turn angle during alignment. */
    private static final int MAX_ANGLE = 120;
    
    private static final int DRIVE_SPEED = 500;
    private static final int ALIGN_SPEED = 200;
    private static final int SEARCH_SWEEP_SPEED = 400;
    
    private static final double P = 0.4;
    
    private State state = State.NOT_LOCKED;
    
    private int driveSpeed = DRIVE_SPEED;
    private int sweepSpeed = SEARCH_SWEEP_SPEED;

    private Side trackingSide = Side.LEFT;
    
    private int sweepTo = Head.degreeToPosition(DETECTION_ANGLE);
    private int turnDir = -1;
    private int turnSpeed = ALIGN_SPEED;
    
    private float angle = 0;
            
    @Override
    protected void doInit() {
        state = State.NOT_LOCKED;
        
        turnDir = trackingSide.getValue();
    }

    @Override
    protected void doRun() {
        final int value = HEAD.getLight();
        
        switch(state) {
            case NOT_LOCKED:
                if (value > DETECTION_THRESHOLD) {
                    // line found, stop engines and align
                    turnDir = HEAD.getPosition() < 0 ? -1 : 1;
                    
                    ENGINE.stop();
                    HEAD.moveTo(0, true);
                    
                    state = State.WAIT_FOR_HEAD;
                } else {
                    if (!HEAD.isMoving()) {
                        HEAD.moveTo(sweepTo, true, sweepSpeed);
                        
                        sweepTo = -sweepTo;
                    }
                }
                break;
            case WAIT_FOR_HEAD:
                if (!HEAD.isMoving()) {
                    state = State.MOVE_TO_LINE;
                }
                break;
            case MOVE_TO_LINE:
                if (value < DETECTION_THRESHOLD) {
                    angle += ENGINE.estimateAngle();
                    
                    if (Math.abs(angle) > MAX_ANGLE) {
                        turnDir *= -1;
                        turnSpeed = ALIGN_SPEED / 2;
                    }
                    alignmentControlLoop(value);
                } else {
                    ENGINE.stop();
                    
                    setFinished();
                }
                break;
        }
    }
    
    private void alignmentControlLoop(final int value) {
        final int error = 1000 - value; // turn only in one direction
        
        final int linear = (int) (error * P);
        
        int out = turnDir * linear;
        out = Utils.clamp(out, -turnSpeed, turnSpeed); // limit to drive speed
        
        // System.out.println("err: " + error + " lin: " + linear + " out: " + out);
        
        ENGINE.rotate(out);
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
    
    public Side getTrackingSide() {
        return trackingSide;
    }
    
    public void setTrackingSide(final Side side) {
        trackingSide = side;
    }
    
    private enum State {
        NOT_LOCKED,
        WAIT_FOR_HEAD,
        MOVE_TO_LINE
    }
}
