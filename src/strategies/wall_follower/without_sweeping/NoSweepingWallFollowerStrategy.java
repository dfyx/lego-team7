package strategies.wall_follower.without_sweeping;

import strategies.Strategy;
import lejos.nxt.Motor;
import static robot.Platform.ENGINE;

public class NoSweepingWallFollowerStrategy extends Strategy{

	@Override
	protected void doInit() {
		ENGINE.move(1000);
	}

	@Override
	protected void doRun() {
		
		System.out.print("\r Left: " + Motor.A.isStalled() + " , " + Motor.B.isStalled());
	}
	

}
