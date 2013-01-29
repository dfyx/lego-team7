package strategies;

public abstract class Strategy {

    private boolean running = false;

    public void run() {
        doRun();
    }

    public boolean isRunning() {
        return running;
    }

    public void init() {
        running = true;

        doInit();
    }

    abstract void doInit();

    abstract void doRun();

    protected void setFinished() {
        running = false;
    }
}
