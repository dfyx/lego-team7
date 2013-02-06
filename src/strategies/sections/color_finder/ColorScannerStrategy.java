package strategies.sections.color_finder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import robot.Platform;
import strategies.Strategy;

public class ColorScannerStrategy extends Strategy {
	public enum ColorName {
		GREEN(0), RED(1), YELLOW(2);
		
		private final int value;

		private ColorName(final int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
	
	public class Color {
		public int value;
		public int position;
		
		public Color(int position, int value) {
			this.position = position;
			this.value = value;
		}
	}
	
	private int numColors;
	private boolean onColor;
	private Color[] colors;
	private List<Integer> samples;
	private int colorsScanned;
	
	public static final int RISING_THRESHOLD = 350;
	public static final int FALLING_THRESHOLD = 250;

	public ColorScannerStrategy(int numColors) {
		this.numColors = numColors;
	}

	@Override
	protected void doInit() {
		colors = new Color[numColors];
		onColor = false;
		samples = new ArrayList<Integer>();
		colorsScanned = 0;
	}

	@Override
	protected void doRun() {
		int currentLightValue = Platform.HEAD.getLight();
		if (onColor) {
			if (currentLightValue < FALLING_THRESHOLD
				&& samples.size() != 0) {

				int average = calculateAverage();
				colors[colorsScanned] = new Color(colorsScanned, average);
				samples.clear();
				
				onColor = false;
				colorsScanned++;
				
				System.out.println("Lost color: " + average);
			} else {
				samples.add(currentLightValue);
			}
		} else {
			if (currentLightValue > RISING_THRESHOLD) {
				System.out.println("Found color");
				onColor = true;
			}
		}
		
		if (colorsScanned == numColors) {
			sortColors();
			setFinished();
		}
	}

	private int calculateAverage() {
		long sum = 0;
		
		for(Integer i : samples) {
			sum += i;
		}
		return (int) (sum / samples.size());
	}
	
	public Color getColor(ColorName name) {
		return colors[name.getValue()];
	}
	
	private void sortColors() {
		if (numColors != 3) {
			throw new IllegalArgumentException("sortColors is not implemented for arbitrary numbers");
		}
		
		if (colors[0].value > colors[1].value) {
			swapColors(0, 1);
		}
		
		if (colors[1].value > colors[2].value) {
			swapColors(1, 2);
		}
		
		if (colors[0].value > colors[1].value) {
			swapColors(0, 1);
		}
	}

	private void swapColors(int i, int j) {
		Color temp = colors[i];
		colors[i] = colors[j];
		colors[j] = temp;
	}
	
	public int getMinimumDistance() {
		int dist = colors[1].value - colors[0].value;
		
		for(int i = 2; i < colors.length; i++) {
			dist = Math.min(dist, colors[i].value - colors[i - 1].value);
		}
		
		return Math.abs(dist);
	}

}
