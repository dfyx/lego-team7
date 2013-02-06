package bluetooth;

import lejos.util.Delay;

public abstract class Device {
	public class ConnectionThread extends Thread {
		private boolean aborted = false;
		private Device device;
		private int connectionTimeout;
		
		public ConnectionThread(Device device, int connectionTimeout) {
			this.device = device;
			this.connectionTimeout = connectionTimeout;
		}

		@Override
		public void run() {
			while(!Gate.getInstance().isConnected()
				  && !aborted) {
				device.tryConnect();
			}

			if (!aborted) {
				Delay.msDelay(connectionTimeout);
				device.disconnect();
			}
		}

		public void abort() {
			aborted  = true;
		}
	}

	private volatile boolean connected = false;
	protected ConnectionThread connectionThread;
	protected int connectionTimeout;
	
	public synchronized void connect() {
		if (!isConnected()) {
			if (connectionThread == null) {
				connectionThread = new ConnectionThread(this, connectionTimeout);
			}
			
			connectionThread.start();
		}
	}
	
	private void tryConnect() {
		connected = doConnect();
	}
	
	protected abstract boolean doConnect();
	
	public synchronized void disconnect() {
		if (isConnected()) {
			if (connectionThread != null) {
				connectionThread.abort();
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}
}
