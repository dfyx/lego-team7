package strategies.sections;

import static robot.Platform.ENGINE;
import static robot.Platform.HEAD;
import lejos.util.Stopwatch;
import strategies.Strategy;
import strategies.line_follower.LineFollowerController;
import strategies.util.TurnAngleStrategy;
import strategies.wall_follower.WallFollowerStrategy;

public class LineToPlantController extends Strategy {

    private State state = State.FIND_LINE;
    
    private TurnAngleStrategy turn = new TurnAngleStrategy();
    
    private Stopwatch time = new Stopwatch();
    private WallFollowerStrategy wall = null;
    private LineFollowerController line = new LineFollowerController();
    
    @Override
    protected void doInit() {
        state = State.TURN_START;
        
        turn.setTargetAngle(3);
        turn.init();
    }

    @Override
    protected void doRun() {
        switch(state) {
            case TURN_START:
                turn.run();
                
                if (turn.isFinished()) {
                    ENGINE.move(300, -200);
                    line.init();
                    line.setState(LineFollowerController.State.OFF_LINE_SEEK);
                    state = State.FOLLOW_LINE;
                }
                break;
            case FOLLOW_LINE:
                line.run();
                
                if (HEAD.getDistance() < 10) {
                    wall = WallFollowerStrategy.getBridgeStartStrategy();
                    wall.init();
                    time.reset();
                    state = State.AVOID_PLANT;
                }
                break;
            case AVOID_PLANT:
                wall.run();
                
                if (time.elapsed() > 2 * 1000 && line.lineValueOk()) {
                    line.setState(LineFollowerController.State.JUST_LOST_LINE);
                    line.init();
                    state = State.DRIVE_TO_BARCODE;
                }
                break;
            case DRIVE_TO_BARCODE:
                line.run();
                
                if (line.isFinished()) {
                    setFinished();
                }
                break;
        }

    }

    
    private static enum State {
        TURN_START,
        FIND_LINE,
        FOLLOW_LINE,
        AVOID_PLANT,
        DRIVE_TO_BARCODE;
    }
}
