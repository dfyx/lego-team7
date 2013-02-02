import static robot.Platform.ENGINE;
import lejos.util.Delay;
import robot.Platform;
import strategies.Strategy;
import utils.Utils;

public class Loop {
	/**
	 * Time the main loop needs per cycle (in ms)
	 */
	private static int LOOP_TIME = 10;
	
	/**
	 * Counts main loop cycles
	 */
	private static int numCycles = 0;
	
	/**
	 * Main loop
	 * Only run this once!
	 * 
	 * @param strategy The strategy to perform
	 * 
	 */
	public void run(Strategy strategy) {
		Utils.resetTimer();
		numCycles = 0;
		
		strategy.init();
		
		int lastEndTime = Utils.getSystemTime();
		
		while(strategy.isRunning()) {
			// poll sensors
			Platform.poll();
			
			// run strategy and commit changes
			strategy.run();
			ENGINE.commit();
			
			// check when to perform the next cycle
			lastEndTime += LOOP_TIME;
			if(lastEndTime > Utils.getSystemTime())
				Delay.msDelay(lastEndTime - Utils.getSystemTime());
			
			numCycles++;
		}
	}
	
	public static int getNumCycles() {
		return numCycles;
	}
}
