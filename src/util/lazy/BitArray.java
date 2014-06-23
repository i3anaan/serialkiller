package util.lazy;

import java.util.Arrays;

public class BitArray extends BitList {
	private int size;
	private long[] words;
	
	public BitArray() {
		this(new long[]{});
	}
	
	public BitArray(long[] words) {
		this.size = words.length * 64;
		this.words = words;
	}
	
	public BitArray(byte[] bytes) {
		this.size = bytes.length * 8;
		int numwords = align(this.size) / 64;
		words = new long[numwords];
		
		for (int i = 0; i < bytes.length; i++) {
			byte b = bytes[i];
			
			int word_idx = i / 8;
			int byte_idx = i % 8;
			
			words[word_idx] |= (b << (byte_idx * 8));
		}
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean get(int i) {
		if (i >= size) throw new IndexOutOfBoundsException();
		
		int word_idx = i / 64;
		int bit_idx = i % 64;
		
		long mask = (1L << 63) >>> bit_idx;
		return (words[word_idx] & mask) != 0;
	}
	
	/** Rounds the index up to the nearest multiple of 64. */
	static int align(int index) {
		return ((index + 63) / 64) * 64;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof BitArray) {
			return Arrays.equals(((BitArray) obj).words, this.words);
		} else {
			return false;
		}
	}

}
