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
		this.strategy=strategy;
	}
	
	public void abort() {
		abort = true;
	}
	
	public boolean isRunning() {
		return isRunning;
	}
	
	/**
	 * Main loop
	 * Only run this once!
	 * 
	 * @param strategy The strategy to perform
	 * 
	 */
	public void run() {
		isRunning = true;
		
		Utils.resetTimer();
		numCycles = 0;
		
		strategy.init();
		
		int lastEndTime = Utils.getSystemTime();
		
		while(strategy.isRunning() && !abort) {
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
		isRunning=false;
	}
	
	public int getNumCycles() {
		return numCycles;
	}
}
