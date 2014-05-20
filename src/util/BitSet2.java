package util;

import java.util.BitSet;

import util.BitSet2;

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
		this.length = Math.max(length, bitIndexFrom+1);
	}
	public void set(int bitIndexFrom, boolean value){
		super.set(bitIndexFrom,value);
		this.length = Math.max(length, bitIndexFrom+1);
	}
	public void set(int bitIndexFrom, int bitIndexTo){
		super.set(bitIndexFrom,bitIndexTo);
		this.length = Math.max(length, bitIndexTo+1);
	}
	public void set(int bitIndexFrom, int bitIndexTo, boolean value){
		super.set(bitIndexFrom,bitIndexTo,value);
		this.length = Math.max(length, bitIndexTo+1);
	}
	
	public int length(){
		return length;
	}
	
	public String toString(){
		String s = "";
		for(int i=0;i<length();i++){
			s = s + (this.get(i) ? "1" : "0");
		}
		
		return s;
	}
}
