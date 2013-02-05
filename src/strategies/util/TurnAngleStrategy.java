package strategies.util;

import static robot.Platform.ENGINE;
import strategies.Strategy;

public class TurnAngleStrategy extends Strategy {

    private State state = State.START;
    
    private int speed = 500;
    private float targetAngle = 0;
    private float currentAngle = 0;
    
    @Override
    protected void doInit() {
        state = State.START;
        
        currentAngle = 0;
    }

    @Override
    protected void doRun() {
        currentAngle += ENGINE.estimateAngle();
        
        switch (state) {
            case START:
                if (targetAngle > 0) {
                    ENGINE.rotate(Math.abs(speed));
                } else {
                    ENGINE.rotate(-Math.abs(speed));
                }
                
                state = State.TURN;
                break;
            case TURN:
                if (Math.abs(currentAngle) > Math.abs(targetAngle)) {
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
     * Sets the turn speed. {@link #init()} must be called after using this
     * method.
     * 
     * @param speed the desired drive speed
     */
    public void setSpeed(final int speed) {
        this.speed = speed;
    }
    
    public float getTargetAngle() {
        return targetAngle;
    }
    
    /**
     * Sets the target angle. {@link #init()} must be called after using this
     * method.
     * 
     * @param targetAngle the desired angle in degrees
     */
    public void setTargetAngle(final float targetAngle) {
        this.targetAngle = targetAngle;
    }
    
    public float getCurrentAngle() {
        return currentAngle;
    }

    private static enum State {
        START,
        TURN;
    }
}
