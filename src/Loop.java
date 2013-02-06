import static robot.Platform.ENGINE;
import lejos.util.Delay;
import robot.Platform;
import strategies.Strategy;
import utils.Utils;

public class Loop extends Thread {
	/**
	 * Time the main loop needs per cycle (in ms)
	 */
	private static int LOOP_TIME = 10;

	private Strategy strategy;

	/**
	 * Counts main loop cycles
	 */
	private int numCycles = 0;

	private boolean isRunning = false;
	private boolean abort = false;

	public Loop(Strategy strategy) {
		this.strategy = strategy;
	}

	public void abort() {
		abort = true;
	}

	public boolean isRunning() {
		return isRunning;
	}

	/**
	 * Main loop Only run this once!
	 * 
	 * @param strategy
	 *            The strategy to perform
	 * 
	 */
	public void run() {
		isRunning = true;

		Utils.resetTimer();
		numCycles = 0;

		strategy.init();

		while (Platform.HEAD.isCalibrating()) {
			Platform.HEAD.run();
			Delay.msDelay(10);
		}

		int nextIterationTime = Utils.getSystemTime();

		while (strategy.isRunning() && !abort) {
			int currentTime = Utils.getSystemTime();
			if (nextIterationTime > currentTime) {
				Delay.msDelay(nextIterationTime - currentTime);
				nextIterationTime += LOOP_TIME;
			} else {
				nextIterationTime = currentTime + LOOP_TIME;
			}

			// Give some computation time to the head
			Platform.HEAD.run();
			// poll sensors
			Platform.poll();

			// run strategy and commit changes
			strategy.run();
			ENGINE.commit();

			numCycles++;
		}
		isRunning = false;
	}

	public int getNumCycles() {
		return numCycles;
	}
}
