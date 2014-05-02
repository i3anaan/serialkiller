package link;

import java.util.BitSet;

import util.BitSets;
import util.ByteArrays;

public class Unit {

	public boolean isSpecial;
	public byte b;

	public static final int FLAG_FILLER_DATA = 1;
	public static final byte IS_SPECIAL_BIT = 1;

	public Unit(byte b) {
		this.b = b;
		isSpecial = false;
	}

	public Unit(byte b, boolean special) {
		this.b = b;
		this.isSpecial = special;
	}

	public Unit(int type) {
		this.isSpecial = true;
		if (type == FLAG_FILLER_DATA) {
			this.b = FLAG_FILLER_DATA;
		}
	}

	public BitSet asBitSet() {
		byte[] arr = new byte[] { b };
		BitSet specialBit = new BitSet();
		specialBit.set(0,true);
		return BitSets.concatenate(ByteArrays.toBitSet(arr), specialBit);
	}
	
	public BitSet dataAsBitSet(){
		if(!isSpecial){
			byte[] arr = new byte[] { b };
			return ByteArrays.toBitSet(arr);
		}else{
			return new BitSet();
		}
	}

	public boolean isDataOrFill() {
		return !isSpecial || b==FLAG_FILLER_DATA;
	}
}
