package strategies.wall_follower.without_sweeping.edge;

import strategies.util.ChildStrategy;
import static robot.Platform.HEAD;
import static robot.Platform.ENGINE;

public class EdgeStrategy extends ChildStrategy {

	@Override
	public boolean willStart() {
		return HEAD.getDistance() >= 255;
	}

	@Override
	public boolean isStopped() {
		// TODO Auto-generated method stub
		return HEAD.getDistance() < 255;
	}

	@Override
	protected void doInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void check() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void work() {
		// TODO Auto-generated method stub
		if(justStarted()) {
			ENGINE.stop();
		}
	}

}
