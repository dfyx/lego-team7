package strategies;

import static robot.Platform.LIGHT_SENSOR;

/**
 * Counts lines/reads barcodes. 
 * 
 * Expected code structure:
 * <li>Clearance
 * <li>Lines
 * <li>Clearance (short)
 * 
 * This strategy requires the HEAD to stay at a fixed position.
 * 
 * @author markus
 */
public class CountLinesStrategy extends Strategy {

    /** Threshold required to detect a rising/falling edge */
    private static final int DELTA_THRESHOLD = 250;
    
    /** Free area before a barcode, in mm. */
    private static final int CLEARANCE_BEFORE = 200;
    /** Free area after a barcode, in mm. */
    private static final int CLEARANCE_AFTER = 75; // 3x 2,5mm
    
    boolean clearance = false;
    boolean rising = false;
    int peak = 1000;
    int edgeCount = 0;
    int lineCount = 0;
    int drivenDistance = 0;
    
    @Override
    protected void doInit() {
        clearance = false;
        rising = false;
        peak = 1000;
        edgeCount = 0;
        lineCount = 0;
        drivenDistance = 0;
    }

    @Override
    protected void doRun() {
        final int value = LIGHT_SENSOR.getValue();
        
        drivenDistance += deltaDistance; // TODO: Read from ENGINE
        
        if (clearance && edgeCount > 0 && drivenDistance > CLEARANCE_AFTER) {
            clearance = false;
            
            if (edgeCount % 2 != 0) {
                // uneven edge count?!
                edgeCount *= -1;
            }
            
            lineCount = edgeCount / 2;
            edgeCount = 0;
        } else if (drivenDistance > CLEARANCE_BEFORE) {
            clearance = true;
            edgeCount = 0;
        }
        
        if (rising) {
            peak = Math.max(peak, value);
        } else {
            peak = Math.min(peak, value);
        }
        
        if (Math.abs(peak - value) > DELTA_THRESHOLD) {
            rising = !rising;
            drivenDistance = 0; // reset distance readings while lines are found
            
            if (clearance) { // count edges only after passing clearance
                edgeCount++;
            }
        }
    }
    
    /** 
     * Checks if a barcode is currently beeing read
     *
     * @return true if a barcode read is in progress
     */
    public boolean isCounting() {
        return clearance && edgeCount > 0;
    }
    
    /**
     * Returns the last read line count. If no barcode has been read, zero is
     * returned. If the last reading has been invalid, the number of found
     * lines is returned as NEGATIVE value. 
     * 
     * @return the last read line count
     */
    public int getLineCount() {
        return lineCount;
    }
}
