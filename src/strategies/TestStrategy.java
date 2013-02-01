package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;
import robot.Platform;
import utils.Utils;

public class TestStrategy extends Strategy {

	protected void doInit() {
		Platform.HEAD.startSweeping(-1000, 1000, 10);
	}

	protected void doRun() {
		System.out.print("\r");
		int[] values = Platform.HEAD.getSweepValues();
		for (int i = 0; i < values.length; ++i)
			System.out.print("" + i + "  \t");
		System.out.flush();
	}
}
