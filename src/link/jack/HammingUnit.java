package link.jack;

import util.BitSet2;
import util.ByteArrays;
import util.Bytes;
import util.encoding.HammingCode;

public class HammingUnit implements Unit{

	public byte b;
	private HammingCode hc;

	public static final byte FLAG_FILLER_DATA = -16; //11110000
	public static final byte FLAG_END_OF_FRAME = -96;//10100000
	public static final byte IS_SPECIAL_BIT = 1;
	//1111000
	
	/**
	 * Use this to Decode from 8 bits
	 * @param data
	 * @param hc
	 */
	public HammingUnit(BitSet2 data,HammingCode hc) {
		this.hc = hc;
		this.b = data.toByteArray()[0];
	}
	
	//4 MSB
	public HammingUnit(byte data, boolean special, HammingCode hc) {
		BitSet2 dataBS = new BitSet2(hc.dataBitCount);
		for(int i =0;i<hc.dataBitCount;i++){
			dataBS.set(i,((data>>>(7-i))&1)==1);
		}
		this.hc = hc;
		this.b =(byte) (hc.encode(dataBS).toByteArray()[0] | (special ? 1 : 0));
	}

	public HammingUnit(BitSet2 data, boolean special, HammingCode hc) {
		this.hc = hc;
		this.b =(byte) (hc.encode(data).toByteArray()[0] | (special ? 1 : 0));
	}

	public BitSet2 fullAsBitSet() {
		byte[] arr = new byte[] { b };
		BitSet2 specialBit = new BitSet2();
		specialBit.set(0,isSpecial());
		return BitSet2.concatenate(BitSet2.valueOf(arr), specialBit);
	}
	
	public BitSet2 dataAsBitSet(){
		if(!isSpecial()){
			return hc.decode(getEncodedPayloadAsBitSet2());
		}else{
			return new BitSet2();
		}
	}

	public boolean isDataOrFill() {
		return !isSpecial() || getDecodedPayload()==FLAG_FILLER_DATA;
	}
	
	public boolean isFiller(){
		return isSpecial() && getDecodedPayload()==FLAG_FILLER_DATA;
	}
	
	
	public String toString(){
		return (isSpecial() ? "F" : "D") +getDecodedPayloadAsBitSet2();
	}
	
	public Unit getClone(){
		return new HammingUnit(getDecodedPayload(),this.isSpecial(),hc);
	}
	
	public boolean equals(Object obj){
		if(obj==null){
			return false;
		}else{
			return (obj instanceof HammingUnit) && ((HammingUnit) obj).b==this.b;
		}
	}

	public boolean isEndOfFrame() {
		return isSpecial()&&getDecodedPayload()==FLAG_END_OF_FRAME;
	}
	
	public boolean isSpecial(){
		return (b&1)==1;
	}
	
	public byte getEncodedPayload(){
		return (byte)(b&-2);
	}
	
	public BitSet2 getEncodedPayloadAsBitSet2(){
		BitSet2 bs2 = new BitSet2(7);
		for(int i=0;i<7;i++){
			bs2.set(bs2.length()-i-1,((b>>(i+1))&1)==1);
		}
		return bs2;
	}
	public byte getDecodedPayload(){
		return hc.decode(getEncodedPayloadAsBitSet2()).toByteArray()[0];
	}
	public BitSet2 getDecodedPayloadAsBitSet2(){
		return hc.decode(getEncodedPayloadAsBitSet2());
	}
	
	public byte getByte(){
		return b;
	}

	@Override
	public Unit getFiller() {
		return new HammingUnit(HammingUnit.FLAG_FILLER_DATA,true ,JackTheRipper.HC);
	}
}
