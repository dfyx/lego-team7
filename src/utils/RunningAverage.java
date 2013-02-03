package utils;

public class RunningAverage {
	private int values[];
	private int currentPos = 0;
	private int numValues = 0;
	private int sum = 0;

	public RunningAverage(int valueCount) {
		values = new int[valueCount];
		for(int i=0;i<valueCount;++i)
			values[i]=0;
	}

	public void addValue(int value) {
		sum -= values[currentPos];
		values[currentPos] = value;
		sum += values[currentPos];
		++currentPos;
		if (currentPos == values.length)
			currentPos = 0;
		
		if(numValues<values.length)
			++numValues;
	}
	
	public int getAverage() {
		return sum/values.length;
	}
}
