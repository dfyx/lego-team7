import lejos.util.Delay;
import sensors.Head;
import strategies.Strategy;
import actors.Engine;

public class Loop {
	
	Engine engine = new Engine();
	Head head = new Head();
	
	public void runStrategies(Strategy strategy) {
		strategy.init(head,engine);
		while(strategy.isRunning()){
			strategy.run();
			engine.commit();
			Delay.msDelay(10);
		}
	}
}
