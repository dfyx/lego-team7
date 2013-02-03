package sensors;

import lejos.nxt.SensorPort;
import utils.Utils;

//Only used inside Head class
class LightSensor implements Sensor {

    private final lejos.nxt.LightSensor realSensor;
    
    private int maxLight;
    private int minLight;
    
    public LightSensor(final SensorPort port) {
        realSensor = new lejos.nxt.LightSensor(port);
        
        resetCalibration();
    }

    /**
     * Returns the light value, normalized between 0 and 1000. Backlight effects
     * are suppressed.
     */
    public int getValue() {
        realSensor.setFloodlight(true);
        final int onValue = realSensor.getNormalizedLightValue();
        
        realSensor.setFloodlight(false);
        final int offValue = realSensor.getNormalizedLightValue();

        final int result =
                Utils.clamp(1000 * (onValue - offValue - minLight)
                        / (maxLight - minLight), 0, 1000);
        
        // FIXME: Disable Debug Output
        System.out.println("On: " + onValue + " Off: " + offValue + " result: " + result);
        
        return result;
    }
    
    public int getRawValue() {
        return realSensor.getNormalizedLightValue();
    }
    
    /**
     * Calibrate the light sensor. The passed values must have been obtained by
     * {@link #getValue()} because of the backlight-subtraction applied there.
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
