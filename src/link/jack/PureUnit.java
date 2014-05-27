package link.jack;

import java.util.Random;

import util.BitSet2;
import util.Bytes;

public class PureUnit extends Unit{

	public boolean isSpecial;
	public byte b;

	public static final byte FLAG_FILLER_DATA = -1; //11111111
	public static final byte FLAG_END_OF_FRAME = -2;
	public static final byte FLAG_DUMMY = -3;
	public static final byte IS_SPECIAL_BIT = 1;

	public PureUnit(byte b, boolean special) {
		this.b = b;
		this.isSpecial = special;
	}
	public boolean isFiller(){
		return isSpecial && b==FLAG_FILLER_DATA;
	}
	
	
	public boolean isEndOfFrame() {
		return isSpecial&&b==FLAG_END_OF_FRAME;
	}

	@Override
	public boolean isSpecial() {
		return isSpecial;
	}

	@Override
	public Unit getFlag(byte flag) {
		return new PureUnit(flag,true);
	}

	@Override
	public BitSet2 serializeToBitSet() {
		byte[] arr = new byte[] { b };
		BitSet2 specialBit = new BitSet2();
		specialBit.set(0,isSpecial);
		return BitSet2.concatenate(BitSet2.valueOf(arr), specialBit);
		//DDDDDDDDDS
	}

	@Override
	public Unit constructFromBitSet(BitSet2 bs) {
		if(bs.length()!=getSerializedBitCount()){
			return null;
		}else{
			return new PureUnit(bs.toByteArray()[0],bs.get(8));
		}
	}

	@Override
	public int getSerializedBitCount() {
		return 9;
	}
	
	@Override
	public PureUnit getRandomUnit(){
		byte[] b = new byte[1];
		JackTheRipper.R.nextBytes(b);
		return new PureUnit(b[0],JackTheRipper.R.nextBoolean());
	}
	
	public String toString(){
		return ("P"+(isSpecial ? "F" : "D")) +Bytes.format(b);
	}
	public boolean equals(Object obj){
		if(obj==null){
			return false;
		}else{
			return (obj instanceof PureUnit) && ((PureUnit) obj).b==this.b && ((PureUnit) obj).isSpecial == this.isSpecial;
		}
	}

	public static PureUnit getDummy(){
		return new PureUnit(FLAG_DUMMY,true);
	}
	@Override
	public Unit getFiller() {
		return getFlag(FLAG_FILLER_DATA);
	}
	@Override
	public Unit getEndOfFrame() {
		return getFlag(FLAG_END_OF_FRAME);
	}
	
	
	@Override
	public String toCoolString(){
		return "PureUnit, much data many compress such error. wow.";
	}
}
