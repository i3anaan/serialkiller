package link.angelmaker.nodes;

import link.jack.HammingUnit;
import link.jack.JackTheRipper;
import link.jack.Unit;
import util.BitSet2;
import util.encoding.HammingCode;


//TODO CLASS IS NOT DONE YET.
public class HammingNode implements Node.Fillable, Node.Leaf{

	public byte stored;
	private HammingCode hc;
	public static final byte FLAG_FILLER_DATA = 1; //00000001
	public static final byte FLAG_END_OF_FRAME = 3;//00000011
	public static final byte IS_SPECIAL_MASK = 1;

	public HammingNode(Node parent, HammingCode hc) {
		this.hc = hc;
		stored = FLAG_FILLER_DATA;
	}		
	private boolean isSpecial(){
		return (stored&IS_SPECIAL_MASK)==1;
	}
	private boolean isEndOfFrame() {
		return isSpecial() && getDecodedPayloadAsByte()==FLAG_END_OF_FRAME;
	}
	
	private byte getEncodedPayloadAsByte(){
		return (byte)(stored&-2);
	}
	
	private BitSet2 getEncodedPayloadAsBitSet(){
		BitSet2 bs2 = new BitSet2(7);
		for(int i=0;i<7;i++){
			bs2.set(bs2.length()-i-1,((stored>>(i+1))&1)==1);
		}
		return bs2;
	}
	private byte getDecodedPayloadAsByte(){
		return hc.decode(getEncodedPayloadAsBitSet()).toByteArray()[0];
	}
	private BitSet2 getDecodedPayloadAsBitSet(){
		return hc.decode(getEncodedPayloadAsBitSet());
	}
	
	private byte getByte(){
		return stored;
	}
	
	public String toString(){
		return ("H"+(isSpecial() ? "F" : "D")) +getDecodedPayloadAsBitSet();
	}
	
	public boolean equals(Object obj){
		if(obj==null){
			return false;
		}else{
			return (obj instanceof HammingNode) && ((HammingNode) obj).stored==this.stored;
		}
	}

	private HammingUnit getRandomUnit() {
		return new HammingUnit(new BitSet2(new boolean[]{JackTheRipper.R.nextBoolean(),JackTheRipper.R.nextBoolean(),JackTheRipper.R.nextBoolean(),JackTheRipper.R.nextBoolean()}),JackTheRipper.R.nextBoolean(),hc);
	}
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		BitSet2 encoded = hc.encode(bits.get(0, 4));
		encoded.set(7,false);
		stored = encoded.toByteArray()[0];
		return bits.get(4, bits.length()-1);
	}

	@Override
	public BitSet2 getOriginal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BitSet2 getConverted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFull() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCorrect() {
		// TODO Auto-generated method stub
		return !hc.hasError(getEncodedPayloadAsBitSet());
	}

	@Override
	public Node getClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFiller() {
		return stored==FLAG_FILLER_DATA;
	}
	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

}
