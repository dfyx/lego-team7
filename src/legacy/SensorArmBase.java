package legacy;
import lejos.nxt.LightSensor;
import lejos.nxt.NXTRegulatedMotor;
import robot.Platform;

public class SensorArmBase {
	
	public LightSensor getLightSensor() {
		return Platform.LIGHT_SENSOR.getRealSensor();
	}
	
	public NXTRegulatedMotor getMotor() {
		//return Platform.HEAD_MOTOR;
		return null;
	}
}
