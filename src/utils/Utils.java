package utils;

public class Utils {
	public static int clamp(int value, int min, int max) {
		return Math.min(max,Math.max(min,value));
	}
}
