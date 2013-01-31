package strategies;

import static robot.Platform.ENGINE;
import static robot.Platform.LIGHT_SENSOR;

import java.util.Arrays;

import utils.Utils;

/**
 * A strategy to calibrate the light sensor.
 * 
 * Concept: The strategy samples measurments during a fixed time while moving.
 * The largest and the smalles samples will be dropped, a portion of the largest/
 * smalles remaining samples will be used to calculate a mean value of the
 * black-point and white-point.
 * 
 * @author markus
 */
public class LightCalibrationStrategy extends Strategy {

    /** Time to sample light sensor. */
	private static final int SEARCH_TIME = 5 * 1000;
	/** Driving speed while sampling light sensor. */
	private static final int BASE_SPEED = 500;
	/** Number of samples to collect. */
	private static final int SAMPLE_SIZE = 200;
	/** Number of largest/smallest samples to ignore. */
	private static final int DROPPED_SAMPLES = 5;
    /**
     * Number of largest/smallest remaining samples to use for black-/whitePoint
     * calculation.
     */
	private static final int ACCEPTED_SAMPLES = 15;
	/** Time per sample. */
	private static final int SAMPLE_TIME = SEARCH_TIME / SAMPLE_SIZE;

	/** Interval start timestamp */
	private int startTime = 0;
	/** The number of currently collected samples */
	private int sampleCount = 0;
    /** Collected samples */
	private int samples[] = new int[SAMPLE_SIZE];

	protected void doInit() {
	    LIGHT_SENSOR.setFloodlight(true);
		// Switch off calibration
		LIGHT_SENSOR.resetCalibration();
		
		startTime = Utils.getSystemTime();
		
		ENGINE.move(BASE_SPEED);
	}

	protected void doRun() {	    
	    if (elapsedTime() > SAMPLE_TIME) {
	        resetTime();
	        
	        final int sampleValue = LIGHT_SENSOR.getRawValue();
	        samples[sampleCount++] = sampleValue;
	        
	        System.out.println("Sample " + sampleCount + ": " + sampleValue);
	        
	        if (sampleCount == SAMPLE_SIZE) {
	            Arrays.sort(samples);

                int blackPoint = 0;
                int whitePoint = 0;
                
	            for (int i = DROPPED_SAMPLES; i < DROPPED_SAMPLES + ACCEPTED_SAMPLES; i++) {
	                blackPoint += samples[i];
	                whitePoint += samples[SAMPLE_SIZE - i - 1];
	            }
	            
	            blackPoint /= ACCEPTED_SAMPLES;
	            whitePoint /= ACCEPTED_SAMPLES;
	            
	            LIGHT_SENSOR.calibrate(blackPoint, whitePoint);
	            
                System.out.println("Min: " + samples[0] + " Max: "
                        + samples[SAMPLE_SIZE - 1] + " bp: " + blackPoint
                        + " wp: " + whitePoint);
	            
	            setFinished();
	        }   
	    }
	}
	
	/**
	 * Reset interval timer.
	 */
	private void resetTime() {
	    startTime = Utils.getSystemTime();
	}
	
	/**
	 * Get the elapsed time since last interval timer reset.
	 * 
	 * @return the elapsed time in ms
	 */
	private int elapsedTime() {
	    return Utils.getSystemTime() - startTime;
	}
}
