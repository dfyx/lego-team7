package strategies;

import static robot.Platform.ENGINE;

public class MoveDistanceStrategy extends Strategy {
   
    private State state = State.START;
    
    private int speed = 500;
    private float targetDistance = 0;
    private float currentDistance = 0;
    
    @Override
    protected void doInit() {
        state = State.START;
        
        currentDistance = 0;
    }

    @Override
    protected void doRun() {
        currentDistance += ENGINE.estimateDistance();
        
        switch (state) {
            case START:
                if (targetDistance > 0) {
                    ENGINE.move(Math.abs(speed));
                } else {
                    ENGINE.move(-Math.abs(speed));
                }
                
                state = State.DRIVE;
                break;
            case DRIVE:
                if (Math.abs(currentDistance) > Math.abs(targetDistance)) {
                    ENGINE.stop();
                    
                    setFinished();
                }
                break;
        }
    }

    public int getSpeed() {
        return speed;
    }
    
    /**
     * Sets the drive speed. {@link #init()} must be called after using this
     * method.
     * 
     * @param speed the desired drive speed
     */
    public void setSpeed(final int speed) {
        this.speed = speed;
    }
    
    public float getTargetPosition() {
        return targetDistance;
    }
    
    /**
     * Sets the target position. {@link #init()} must be called after using this
     * method.
     * 
     * @param targetPosition the desired position in mm
     */
    public void setTargetPosition(final float targetPosition) {
        targetDistance = targetPosition;
    }
    
    public float getCurrentDistance() {
        return currentDistance;
    }
    
    private static enum State {
        START,
        DRIVE;
    }
}
