import lejos.nxt.LightSensor;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.SensorPort;

public class SensorArmBase {
	private LightSensor lightSensor = new LightSensor(SensorPort.S1);
	private NXTRegulatedMotor motor = Motor.C;
	
	public LightSensor getLightSensor() {
		return lightSensor;
	}
	
	public NXTRegulatedMotor getMotor() {
		return motor;
	}
}
