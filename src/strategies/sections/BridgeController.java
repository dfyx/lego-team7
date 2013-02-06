package strategies.sections;

import strategies.Strategy;
import strategies.wall_follower.WallFollowerStrategy;

public class BridgeController extends Strategy {

    private State state = State.ALIGN_WALL;
    private WallFollowerStrategy wall = null;
    private BridgeStrategy bridge = new BridgeStrategy();
    
    @Override
    protected void doInit() {
        state = State.ALIGN_WALL;
        wall = WallFollowerStrategy.getBridgeStartStrategy();
        wall.init();
    }

    @Override
    protected void doRun() {
        switch(state) {
            case ALIGN_WALL:
                wall.run();
                
                if (wall.getLostEdgeCount() > 0) {
                    bridge.init();
                    state = State.FOLLOW_BRIDGE;
                }
                break;
            case FOLLOW_BRIDGE:
                bridge.run();
                
                if (bridge.isFinished()) {
                    setFinished();
                }
                break;
        }

    }

    
    private static enum State {
        ALIGN_WALL,
        FOLLOW_BRIDGE
    }
}
