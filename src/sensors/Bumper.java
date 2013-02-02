package sensors;

import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

public class Bumper {

    private final TouchSensor realSensor;
    
    private boolean sensorValue;
    
    public Bumper(final SensorPort port) {
        realSensor = new TouchSensor(port);
    }
    
    public void poll() {
        sensorValue = realSensor.isPressed();
    }

    public boolean getValue() {
        return sensorValue;
    }
    
    public TouchSensor getRealSensor() {
        return realSensor;
    }

}
