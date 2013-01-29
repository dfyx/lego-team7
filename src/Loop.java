import static robot.Platform.ENGINE;
import static robot.Platform.SENSORS;
import lejos.util.Delay;
import sensors.Sensor;
import strategies.Strategy;

public class Loop {
		
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
