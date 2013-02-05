package strategies;

public abstract class Strategy implements Action {

	private boolean running = false;

	@Override
	public void run() {
		doRun();
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isFinished() {
		return !running;
	}

	public void init() {
		running = true;

		doInit();
	}

	abstract protected void doInit();

	abstract protected void doRun();

	protected void setFinished() {
		running = false;
	}
}
