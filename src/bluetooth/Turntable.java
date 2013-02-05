package bluetooth;

public class Turntable extends Device {
	public static final Turntable INSTANCE = new Turntable();
	private static final int TIMEOUT = 2 * 60 * 1000;
	private TurnControl turnControl = new TurnControl();
	
	private Turntable() {
		connectionTimeout = TIMEOUT;
	}

	public static Turntable getInstance() {
		return INSTANCE;
	}

	public synchronized void disconnect() {
		if (isConnected()) {
			super.disconnect();
			turnControl.disconnectFromTurntable();
		}
	}
	
	public synchronized void rotate(int angle) {
		if (!isConnected()) {
			throw new IllegalStateException("Cannot rotate turntable while disconnected");
		}
		turnControl.turnClockwise(angle);
	}

	protected boolean doConnect() {
		return turnControl.connectionToTurntableSuccessful();
	}
}
