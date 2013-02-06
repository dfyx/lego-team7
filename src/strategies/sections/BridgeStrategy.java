package strategies.sections;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import lejos.util.Stopwatch;
import sensors.Head;
import strategies.Strategy;
import strategies.line_follower.LineFollowerStrategy;
import utils.Utils.Side;

public class BridgeStrategy extends Strategy {

    private static final int BRIDGE_THRESHOLD = 250;
    private static final int SPEED = 300;
    private static final int CLAMP = 1000;
    private static final int ABS_DIRECTION = 50;
    
    private static final int MIN_FOLLOW_SECONDS = 5;
    
    private State state = State.SEEK_BRIDGE;
    
    private Side side = Side.LEFT;
    
    private Stopwatch timer = new Stopwatch();
    
    private LineFollowerStrategy line = new LineFollowerStrategy(LineFollowerStrategy.BRIDGE_PARAMS);
    
    @Override
    protected void doInit() {
        State.SEEK_BRIDGE.transitionTo(this);
    }

    @Override
    protected void doRun() {
        final int value = HEAD.getLight();
        
        State oldState = state;
        
        switch (state) {
            case SEEK_BRIDGE:
                if (HEAD.isMoving()) {
                    return;
                }
                
                if (value > BRIDGE_THRESHOLD) {
                    State.SEEK_EDGE.transitionTo(this);
                }
                break;
            case SEEK_EDGE:
                if (value < BRIDGE_THRESHOLD) {
                    State.FOLLOW_EDGE.transitionTo(this);
                }
                break;
            case FOLLOW_EDGE:
                line.run();
                
                if (line.isFinished()) {
                    if (timer.elapsed() < MIN_FOLLOW_SECONDS * 1000) {
                        line.init();
                    } else {
                        State.ROTATE_HEAD.transitionTo(this);
                    }
                }
                break;
            case ROTATE_HEAD:
                if (!HEAD.isMoving()) {
                    setFinished();
                }
                break;
        }
    }

    public static enum State {
        SEEK_BRIDGE {
            @Override
            void doTransitionTo(final BridgeStrategy ctrl) {
                HEAD.moveTo(Head.degreeToPosition(90) * ctrl.side.getValue(), 1000);
                ENGINE.move(SPEED, ctrl.side.getValue() * ABS_DIRECTION);
            }
        },
        SEEK_EDGE {
            @Override
            void doTransitionTo(final BridgeStrategy ctrl) {
            }
        },
        FOLLOW_EDGE {
            @Override
            void doTransitionTo(final BridgeStrategy ctrl) {
                ctrl.timer.reset();
                
                ctrl.line.init();
                
                ctrl.line.setSpeed(SPEED);
                ctrl.line.setClamp(CLAMP);
            }
        },
        ROTATE_HEAD {
            @Override
            void doTransitionTo(final BridgeStrategy ctrl) {
                HEAD.moveTo(Head.degreeToPosition(0 * ctrl.side.getValue() * -1), SPEED);
            }
        };
        
        abstract void doTransitionTo(final BridgeStrategy ctrl);
        
        void transitionTo(final BridgeStrategy ctrl) {
            ctrl.state = this;
            
            doTransitionTo(ctrl);
        }
    }
}
