package util.lazy;

import java.util.Iterator;

public abstract class BitList implements Iterable<Boolean> {
	protected class BitListIterator implements Iterator<Boolean> {
		private BitList bl;
		private int size;
		private int i;
		
		BitListIterator(BitList bl) {
			this.bl = bl;
			this.size = bl.size();
			this.i = 0;
		}

		public boolean hasNext() {
			return size >= this.i;
		}

		public Boolean next() {
			Boolean b = bl.get(i);
			i++;
			return b;
		}
		
		public void remove() {
			throw new UnsupportedOperationException("We're an immutable collection.");
		}
	}
	
	public Iterator<Boolean> iterator() {
		return new BitList.BitListIterator(this);
	}
	
	public abstract int size();
	public abstract boolean get(int i);
}
