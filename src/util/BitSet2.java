package util;

import java.util.BitSet;

public class BitSet2 extends BitSet {

	private int length; //actually works
	
	public BitSet2(){
		super();
		this.length = 0;
	}
	
	public BitSet2(int length){
		super();
		this.length = length;
	}
	
	public void set(int bitIndexFrom){
		super.set(bitIndexFrom);
		this.length = Math.max(length, bitIndexFrom);
	}
	public void set(int bitIndexFrom, boolean value){
		super.set(bitIndexFrom,value);
		this.length = Math.max(length, bitIndexFrom);
	}
	public void set(int bitIndexFrom, int bitIndexTo){
		super.set(bitIndexFrom,bitIndexTo);
		this.length = Math.max(length, bitIndexTo);
	}
	public void set(int bitIndexFrom, int bitIndexTo, boolean value){
		super.set(bitIndexFrom,bitIndexTo,value);
		this.length = Math.max(length, bitIndexTo);
	}
}
