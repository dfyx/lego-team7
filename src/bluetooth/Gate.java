package bluetooth;

public class Gate extends Device {
	public static final Gate INSTANCE = new Gate();
	private static final int TIMEOUT = 2 * 60 * 1000;
	private GateControl gateControl = new GateControl();
	
	private Gate() {
		connectionTimeout = TIMEOUT;
	}

	public static Gate getInstance() {
		return INSTANCE;
	}

	public synchronized void disconnect() {
		if (isConnected()) {
			super.disconnect();
			gateControl.disconnectFromGate();
		}
	}
	
	public synchronized void open() {
		if (!isConnected()) {
			throw new IllegalStateException("Cannot open gate while disconnected");
		}
		gateControl.openGate();
	}

	protected boolean doConnect() {
		return gateControl.connectionToGateSuccessful();
	}
}
