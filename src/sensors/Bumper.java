package sensors;

import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;

public class Bumper implements Sensor<Boolean> {

    private final TouchSensor realSensor;
    
    private boolean sensorValue;
    
    public Bumper(final SensorPort port) {
        realSensor = new TouchSensor(port);
    }
    
    @Override
    public void poll() {
        sensorValue = realSensor.isPressed();
    }

    @Override
    public Boolean getValue() {
        return sensorValue;
    }
    
    public TouchSensor getRealSensor() {
        return realSensor;
    }

}
