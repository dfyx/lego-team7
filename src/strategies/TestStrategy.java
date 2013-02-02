package strategies;

import robot.Platform;

public class TestStrategy extends Strategy {

	protected void doInit() {
		Platform.HEAD.startSweeping(-1000, 1000, 20, 10);
	}

	protected void doRun() {
		System.out.print("\r");
		int[] values = Platform.HEAD.getLightSweepValues();
		for (int i = 0; i < values.length; ++i)
			if(values[i]==Integer.MAX_VALUE)
				System.out.print("-  \t");
			else
				System.out.print("" + values[i] + "  \t");
		System.out.flush();
	}
}
