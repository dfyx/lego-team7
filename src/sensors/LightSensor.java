package sensors;

import lejos.nxt.SensorPort;
import utils.Utils;

public class LightSensor implements Sensor<Integer> {

    private final lejos.nxt.LightSensor realSensor;
    
    private int maxLight;
    private int minLight;
    
    private int rawSensorValue;
    private int normalizedValue;
    
    public LightSensor(final SensorPort port) {
        realSensor = new lejos.nxt.LightSensor(port);
        
        resetCalibration();
    }
    
    @Override
    public void poll() {
        rawSensorValue = realSensor.getNormalizedLightValue();
        
        normalizedValue = Utils.clamp(1000 * (rawSensorValue - minLight) / (maxLight - minLight),0,1000);
    }

    /**
     * Returns the light value, normalized between 0 and 1000.
     */
    @Override
    public Integer getValue() {
        return normalizedValue;
    }
    
    public Integer getRawValue() {
        return rawSensorValue;
    }
    
    public lejos.nxt.LightSensor getRealSensor() {
        return realSensor;
    }
    
    public void setFloodlight(boolean value) {
        realSensor.setFloodlight(value);
    }
    
    /**
     * Calibrate the light sensor
     * 
     * @param minValue The value mapped to 0
     * @param maxValue The value mapped to 1000
     */
    public void calibrate(int minValue, int maxValue) {
        minLight = minValue;
        maxLight = maxValue;
    }
    
    public void resetCalibration() {
        maxLight = 1023; // maximum sensor value according to lejos.nxt.LightSensor
        minLight = 0;
    }
}
