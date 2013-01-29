package strategies;

import sensors.Head;
import actors.Engine;

public abstract class Strategy {
	
	private boolean running=false;
	protected Head head;
	protected Engine engine;
	
	public void run() {
		doRun();
	}
	
	public boolean isRunning() {
		return running;
	}
	
	public void init(Head head, Engine engine) {
		this.head=head;
		this.engine=engine;
		running=true;
		doInit();
	}
	
	abstract void doInit();
	abstract void doRun();
	protected void setFinished() {
		running=false;
	}
}
