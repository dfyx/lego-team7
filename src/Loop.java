import static robot.Platform.ENGINE;
import static robot.Platform.SENSORS;
import lejos.util.Delay;
import sensors.Sensor;
import strategies.Strategy;
import utils.Utils;

public class Loop {
	/**
	 * Time the main loop needs per cycle (in ms)
	 */
	private static int LOOP_TIME = 10;
	
	/**
	 * Main loop
	 * Only run this once!
	 * 
	 */
	public void loop() {
		Utils.resetTimer();
		int lastEndTime = 0;
		
		boolean run = true;
		while(run) {
			System.out.println("lastEnd: " + lastEndTime + ", " + "current: " + Utils.getSystemTime());
			// ----------------------
			
			// TODO your code here
			
			// ----------------------
			// check when to perform the next cycle
			lastEndTime += LOOP_TIME;
			if(lastEndTime > Utils.getSystemTime())
				Delay.msDelay(lastEndTime - Utils.getSystemTime());
		}
	}
	
	// TODO delete?
	public void runStrategies(Strategy strategy) {
		strategy.init();
		
		while(strategy.isRunning()){
			for (Sensor<?> s : SENSORS) {
			    s.poll();
			}
		    
		    strategy.run();
			
			ENGINE.commit();
			
			Delay.msDelay(10);
		}
	}
}
