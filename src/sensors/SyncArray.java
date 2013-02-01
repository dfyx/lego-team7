package sensors;

//An array used to synchronized sweep value arrays.
//This class is only used inside of SweepThread class.
class SyncArray {
	private int[] data = new int[0];

	public synchronized int[] getCopy() {
		int[] copy = new int[data.length];
		for (int i = 0; i < data.length; ++i) {
			copy[i] = data[i];
		}
		return copy;
	}

	public synchronized void init(int size) {
		data = new int[size];
		for (int i = 0; i < size; ++i) {
			data[i] = Integer.MAX_VALUE;
		}
	}
	
	public synchronized void write(int index, int value) {
		data[index]=value;
	}
	
	public synchronized int size() {
		return data.length;
	}
}

