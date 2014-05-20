package link.jack;

import java.util.Arrays;
import util.BitSet2;
import util.ByteArrays;
import util.Bytes;

public class PureUnit {

	public boolean isSpecial;
	public byte b;

	public static final int FLAG_FILLER_DATA = -1; //11111111
	public static final int FLAG_END_OF_FRAME = -2;
	public static final byte IS_SPECIAL_BIT = 1;

	public PureUnit(byte b) {
		this.b = b;
		isSpecial = false;
	}

	public PureUnit(byte b, boolean special) {
		//System.out.println(Thread.currentThread().getId()+"  New Unit: "+Bytes.format(b)+"  Special: "+special);
		this.b = b;
		this.isSpecial = special;
	}

	public PureUnit(int type) {
		this.isSpecial = true;
		if (type == FLAG_FILLER_DATA) {
			this.b = FLAG_FILLER_DATA;
		}else if(type==FLAG_END_OF_FRAME){
			this.b = FLAG_END_OF_FRAME;
		}
	}

	public BitSet2 asBitSet() {
		byte[] arr = new byte[] { b };
		BitSet2 specialBit = new BitSet2();
		specialBit.set(0,isSpecial);
		return BitSet2.concatenate(BitSet2.valueOf(arr), specialBit);
	}
	
	public BitSet2 dataAsBitSet(){
		if(!isSpecial){
			byte[] arr = new byte[] { b };
			return BitSet2.valueOf(arr);
		}else{
			return new BitSet2();
		}
	}

	public boolean isDataOrFill() {
		return !isSpecial || b==FLAG_FILLER_DATA;
	}
	
	public boolean isFiller(){
		return isSpecial && b==FLAG_FILLER_DATA;
	}
	
	
	public String toString(){
		return (isSpecial ? "F" : "D") +Bytes.format(b);
	}
	
	public PureUnit getClone(){
		return new PureUnit(b,isSpecial);
	}
	
	public boolean equals(Object obj){
		if(obj==null){
			return false;
		}else{
			return (obj instanceof PureUnit) && ((PureUnit) obj).b==this.b && ((PureUnit) obj).isSpecial == this.isSpecial;
		}
	}

	public boolean isEndOfFrame() {
		return isSpecial&&b==FLAG_END_OF_FRAME;
	}
}
