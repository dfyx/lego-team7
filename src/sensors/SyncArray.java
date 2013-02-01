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
		System.out.println("Init "+size);
		System.out.flush();
		data = new int[size];
		for (int i = 0; i < size; ++i) {
			data[i] = Integer.MAX_VALUE;
		}
		System.out.println("inited");
		System.out.flush();
	}
	
	public synchronized void write(int index, int value) {
		System.out.println("write "+index);
		System.out.flush();
		data[index]=value;
	}
	
	public synchronized int size() {
		return data.length;
	}
}

