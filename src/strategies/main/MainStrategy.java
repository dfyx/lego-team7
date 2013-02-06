package strategies.main;
import strategies.Strategy;

public abstract class MainStrategy extends Strategy {	
	public abstract void disableBarcodeDetection();
	public abstract void enableBarcodeDetection();
	public abstract void setClearance();
	
	public abstract void restoreLightCalibration();
}