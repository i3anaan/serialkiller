package link;

import java.util.Arrays;
import util.BitSet2;

import util.BitSets;
import util.ByteArrays;
import util.Bytes;

public class Unit {

	public boolean isSpecial;
	public byte b;

	public static final int FLAG_FILLER_DATA = -1;
	public static final byte IS_SPECIAL_BIT = 1;

	public Unit(byte b) {
		this.b = b;
		isSpecial = false;
	}

	public Unit(byte b, boolean special) {
		//System.out.println(Thread.currentThread().getId()+"  New Unit: "+Bytes.format(b)+"  Special: "+special);
		this.b = b;
		this.isSpecial = special;
	}

	public Unit(int type) {
		this.isSpecial = true;
		if (type == FLAG_FILLER_DATA) {
			this.b = FLAG_FILLER_DATA;
		}
	}

	public BitSet2 asBitSet() {
		byte[] arr = new byte[] { b };
		BitSet2 specialBit = new BitSet2();
		specialBit.set(0,isSpecial);
		return BitSets.concatenate(ByteArrays.toBitSet(arr), specialBit);
	}
	
	public BitSet2 dataAsBitSet(){
		if(!isSpecial){
			byte[] arr = new byte[] { b };
			return ByteArrays.toBitSet(arr);
		}else{
			return new BitSet2();
		}
	}

	public boolean isDataOrFill() {
		return !isSpecial || b==FLAG_FILLER_DATA;
	}
	
	public String toString(){
		return (isSpecial ? "F" : "D") +Bytes.format(b);
	}
}
