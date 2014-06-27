package util.lazy;

public class ConcatenatedBitList extends BitList {
	private BitList[] parts;
	
	ConcatenatedBitList(BitList... in) {
		this.parts = in;
	}

	@Override
	public int size() {
		int total = 0;
		for (BitList part : parts) total += part.size();
		return total;
	}

	@Override
	public boolean get(int i) {
		for (BitList part : parts) {
			if (i < part.size()) {
				return part.get(i);
			} else {
				i -= part.size();
			}
		}
		
		throw new IndexOutOfBoundsException();
	}

}
