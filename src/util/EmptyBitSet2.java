package util;

import java.util.BitSet;

/**
 * Immutable empty bitset2 using singleton pattern to save memory.
 * @author I3anaan
 *
 */
public class EmptyBitSet2 extends BitSet2 {
	private static final long serialVersionUID = 1L;
	private static EmptyBitSet2 instance;
	
	private EmptyBitSet2(){
	}
	
	public static EmptyBitSet2 getInstance(){
		if(instance==null){
			instance = new EmptyBitSet2();
		}
		return instance;
	}
	
	@Override
	public void set(int bitIndexFrom){
		throw new UnsupportedOperationException();
	}
	@Override
	public void set(int bitIndexFrom, boolean value){
		throw new UnsupportedOperationException();
	}
	@Override
	public void set(int bitIndexFrom, int bitIndexTo){
		throw new UnsupportedOperationException();
	}
	@Override
	public void set(int bitIndexFrom, int bitIndexTo, boolean value){
		throw new UnsupportedOperationException();
	}
	@Override
	public int length(){
		return 0;
	}
	
	/**
	 * functions as length() method of BitSet.
	 */
	@Override
	public int notLengthButLastBitSet(){
		return super.length();
	}
	@Override
	public String toString(){
		return "[EmptyBitSet2]";
	}
	
	@Override
	public BitSet2 get(int fromIndex, int toIndex){
		return this;
	}
	@Override
	public Object clone(){
		return this;
	}
	
	@Override
	public void clear(){
	}
	@Override
	public void addAtEnd(boolean bit){
		throw new UnsupportedOperationException();
	}
	@Override
	public void addAtEnd(BitSet2 bits){
		throw new UnsupportedOperationException();
	}
	@Override
	public void insert(int index, boolean b) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void insert(int startIndex, BitSet2 bits) {
		throw new UnsupportedOperationException();
	}
	@Override
	public void remove(int index) {
	}
	@Override
	public void remove(int startIndex, int endIndex) {
	}
}
