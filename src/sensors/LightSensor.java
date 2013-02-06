package sensors;

import lejos.nxt.SensorPort;
import utils.Utils;

//Only used inside Head class
class LightSensor implements Sensor {

    public static final CalibrationData DEFAULT_CALIBRATION = new CalibrationData(0, 1023); // maximum sensor value according to lejos.nxt.LightSensor
    
    private final lejos.nxt.LightSensor realSensor;
    
    private CalibrationData calibration;
    
    public LightSensor(final SensorPort port) {
        realSensor = new lejos.nxt.LightSensor(port);
        
        resetCalibration();
        setFloodlight(true);
    }

    /**
     * Returns the light value, normalized between 0 and 1000.
     */
    public int getValue() {
        return Utils.clamp(1000
                * (realSensor.getNormalizedLightValue() - calibration.minLight)
                / (calibration.maxLight - calibration.minLight), 0, 1000);
    }
    
    public int getRawValue() {
        return realSensor.getNormalizedLightValue();
    }
    
    public void setFloodlight(boolean value) {
        realSensor.setFloodlight(value);
    }
    
    public CalibrationData getCalibration() {
        return calibration;
    }
    
    /**
     * Calibrate the light sensor
     * 
     * @param minValue The value mapped to 0
     * @param maxValue The value mapped to 1000
     */
    public void calibrate(int minValue, int maxValue) {
        calibration = new CalibrationData(minValue, maxValue);
    }
    
    public void resetCalibration() {
        calibration = DEFAULT_CALIBRATION;
    }
    
    public static class CalibrationData {
        public final int minLight;
        public final int maxLight;
        
        public CalibrationData(final int minValue, final int maxValue) {
            minLight = minValue;
            maxLight = maxValue;
        }
    }
}
