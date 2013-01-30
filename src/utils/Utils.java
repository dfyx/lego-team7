package utils;

import lejos.util.Stopwatch;

public class Utils {
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
