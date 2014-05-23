package link.jack;

import util.BitSet2;
import util.Bytes;
import util.encoding.HammingCode;

public class HammingUnit extends Unit{

	public byte b;
	private HammingCode hc;
	public static final byte FLAG_DUMMY = -112; //10010000
	public static final byte FLAG_FILLER_DATA = -16; //11110000
	public static final byte FLAG_END_OF_FRAME = -96;//10100000
	public static final byte IS_SPECIAL_BIT = 1;

	public HammingUnit(BitSet2 data, boolean special, HammingCode hc) {
		this.hc = hc;
		this.b =(byte) ((hc.encode(data).toByteArray()[0] & -2) | (special ? 1 : 0));
	}

	public boolean isFiller(){
		return isSpecial() && getDecodedPayloadAsByte()==FLAG_FILLER_DATA;
	}
		
	public boolean isSpecial(){
		return (b&1)==1;
	}
	
	@Override
	public boolean isEndOfFrame() {
		return isSpecial() && getDecodedPayloadAsByte()==FLAG_END_OF_FRAME;
	}
	
	public byte getEncodedPayloadAsByte(){
		return (byte)(b&-2);
	}
	
	public BitSet2 getEncodedPayloadAsBitSet(){
		BitSet2 bs2 = new BitSet2(7);
		for(int i=0;i<7;i++){
			bs2.set(bs2.length()-i-1,((b>>(i+1))&1)==1);
		}
		return bs2;
	}
	public byte getDecodedPayloadAsByte(){
		return hc.decode(getEncodedPayloadAsBitSet()).toByteArray()[0];
	}
	public BitSet2 getDecodedPayloadAsBitSet(){
		return hc.decode(getEncodedPayloadAsBitSet());
	}
	
	public byte getByte(){
		return b;
	}

	@Override
	public Unit getFlag(byte flag) {
		return new HammingUnit(new BitSet2(flag),true ,JackTheRipper.HC);
	}

	@Override
	public BitSet2 serializeToBitSet() {
		return BitSet2.concatenate(getEncodedPayloadAsBitSet(), new BitSet2(new boolean[]{isSpecial()}));
	}

	@Override
	public Unit constructFromBitSet(BitSet2 bs) {
		//bs has length 8, should work with decode() (should ignore extra bits);
		return new HammingUnit(hc.decode(hc.getCorrected(bs)),bs.get(7),hc);
	}

	@Override
	public int getSerializedBitCount() {
		return 8;
	}
	
	public String toString(){
		return ("H"+(isSpecial() ? "F" : "D")) +getDecodedPayloadAsBitSet();
	}
	
	public boolean equals(Object obj){
		if(obj==null){
			return false;
		}else{
			return (obj instanceof HammingUnit) && ((HammingUnit) obj).b==this.b;
		}
	}

	@Override
	public HammingUnit getRandomUnit() {
		return new HammingUnit(new BitSet2(new boolean[]{JackTheRipper.R.nextBoolean(),JackTheRipper.R.nextBoolean(),JackTheRipper.R.nextBoolean(),JackTheRipper.R.nextBoolean()}),JackTheRipper.R.nextBoolean(),hc);
	}

	
	public static HammingUnit getDummy(){
		return new HammingUnit(new BitSet2(FLAG_DUMMY),true ,JackTheRipper.HC);
	}

	@Override
	public Unit getFiller() {
		return getFlag(FLAG_FILLER_DATA);
	}
	@Override
	public Unit getEndOfFrame() {
		return getFlag(FLAG_END_OF_FRAME);
	}
	
}
