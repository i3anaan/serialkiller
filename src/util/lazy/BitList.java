package util.lazy;

import java.util.Iterator;

/**
 * An abstract superclass for an immutable, fixed-size, possibly lazy, 
 * collection of bits.
 */
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
	
	@Override
	public String toString() {
		byte[] barr = new byte[size()+2];
		barr[0] = '[';
		barr[size()+1] = ']';
		
		for (int i = 0; i < size(); i++) {
			barr[i+1] = (byte)(get(i) ? '1' : '0');
		}
		
		return new String(barr);
	}
	
	@Override
	public boolean equals(Object obj) {
        if (obj instanceof BitList) {
            BitList that = (BitList)obj;
            if (this.size() == that.size()) {
	            for (int i = 0; i < that.size(); i++) {
	            	if (this.get(i) != that.get(i)) {
	            		return false;
	            	}
	            }
	            
	            return true;
            } else return false;
        } else return false;
	}
}
