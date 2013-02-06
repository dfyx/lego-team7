package bluetooth;

public class ColorGate extends Device {
	public static final ColorGate INSTANCE = new ColorGate();
	private static final int TIMEOUT = 2 * 60 * 1000;
	private ColorGateControl colorGateControl = new ColorGateControl();
	
	private ColorGate() {
		connectionTimeout = TIMEOUT;
	}

	public static ColorGate getInstance() {
		return INSTANCE;
	}

	public synchronized void disconnect() {
		if (isConnected()) {
			super.disconnect();
			colorGateControl.disconnectFromGate();
		}
	}
	
	public synchronized int readColor() {
		if (!isConnected()) {
			throw new IllegalStateException("Cannot open gate while disconnected");
		}
		return colorGateControl.readColor();
	}

	protected boolean doConnect() {
		return colorGateControl.connectionToColorGateSuccessful();
	}
}
