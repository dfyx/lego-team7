package utils;

import lejos.util.Stopwatch;

public class Utils {
	
    /**
     * An enum representing left/right hand sides.
     */
    public static enum Side {
    	RIGHT(1), 
    	LEFT(-1);
    	
    	private final int value;
    	
    	private Side(final int value) {
    	    this.value = value;
        }
    	
        /**
         * Returns a directional value conforming to {@code robot.Engine} and
         * {@code sensors.Head}.
         * 
         * @return {@code -1} for {@code LEFT} and {@code 1} for {@code RIGHT}
         */
        public int getValue() {
            return value;
        }
    }

    private static Stopwatch watch = new Stopwatch();
	
	public static int clamp(int value, int min, int max) {
		return Math.min(max,Math.max(min,value));
	}
	
	/**
	 * Only call once on program start!
	 */
	public static void resetTimer() {
		watch.reset();
	}
	
	/**
	 * Counts since start of main loop.
	 * Only runs for 25 days :-P
	 * @return system time in ms
	 */
	public static int getSystemTime() {
		return watch.elapsed();
	}
}
