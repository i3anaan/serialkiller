package link.angelmaker.nodes;

import link.angelmaker.AngelMaker;
import util.BitSet2;

/**
 * A Node that flags its data, has 1 childNode.
 * It reads a bit stream, consumes till it spots a start of frame flag.
 * Then start reading data after that flag, till it spots an end of frame flag.
 * It collects bits till it is full (got dataBitCount bits).
 * Only when it detects its full, it gives the data to its childNode.
 * When not full, getOriginal() returns an empty BitSet2.
 * 
 * Best to use either giveConverted or giveOriginal, not both.
 * 
 * @author I3anaan
 *
 */
public class FlaggingNode implements Node,Node.Internal,Node.Fillable{

	private Node[] childNodes;
	private Node parent;
	public static final Flag FLAG_START_OF_FRAME = new BasicFlag(new BitSet2("10011001"));//TODO
	public static final Flag FLAG_END_OF_FRAME = new BasicFlag(new BitSet2("01100110"));//TODO
	//Original, unstuffed, unflagged data.
	private BitSet2 stored;
	private BitSet2 storedConverted;
	private int dataBitCount;
	
	private boolean receivedStartFlag = false;
	private boolean isFull;
	private BitSet2 lastJunk;
	
	
	public FlaggingNode(Node parent, Node child,int dataBitCount){
		childNodes = new Node[]{child};
		this.dataBitCount = dataBitCount;
		this.parent = parent;
	}
	

	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		int i;
		for (i = 0; i < bits.length() && stored.length() < dataBitCount; i++) {
			stored.addAtEnd(bits.get(i));
		}
		if(stored.length()>=dataBitCount){
			isFull =true;
		}
		
		if(isFull){
			BitSet2 remaining = childNodes[0].giveOriginal(getConverted());
			if(remaining.length()>0){
				AngelMaker.logger.alert("FlaggningNode is spilling data");
			}
		}
		
		return bits.get(i, bits.length());
	}

	@Override
	public BitSet2 getOriginal() {
		return childNodes[0].getOriginal();
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		if(!receivedStartFlag){
			BitSet2 afterStart = getDataAfterStartFlag(bits);
			if(afterStart.length()>0){
				storedConverted = afterStart;
			}
		}else{
			storedConverted = BitSet2.concatenate(storedConverted, bits);
		}
		
		int contains = storedConverted.contains(FLAG_END_OF_FRAME.getFlag());
		if(receivedStartFlag && contains>0){
			//Received start and end flag.
			isFull = true;
			stored = new BitSet2();
			giveOriginal(unStuff(removeFlags(bits)));
			return storedConverted.get(contains+FLAG_END_OF_FRAME.getFlag().length(), storedConverted.length());
		}else{
			//Does not have end flag yet.
			return new BitSet2();
		}
	}
	
	private BitSet2 placeFlags(BitSet2 bits){
		return BitSet2.concatenate(BitSet2.concatenate(FLAG_START_OF_FRAME.getFlag(), bits),FLAG_END_OF_FRAME.getFlag());
		
	}
	private BitSet2 removeFlags(BitSet2 bits){
		return getDataBeforeEndFlag(getDataAfterStartFlag(bits));
	}
	private BitSet2 stuff(BitSet2 bits){
		BitSet2 result = (BitSet2) bits.clone();
		Flag[] flags = new Flag[]{FLAG_START_OF_FRAME,FLAG_END_OF_FRAME};
		for(int i = 0;i<flags.length;i++){
			flags[i].stuff(result);
		}
		return result;
	}
	private BitSet2 unStuff(BitSet2 bits){
		BitSet2 result = (BitSet2) bits.clone();
		Flag[] flags = new Flag[]{FLAG_START_OF_FRAME,FLAG_END_OF_FRAME};
		for(int i = flags.length-1;i>=0;i--){
			flags[i].unStuff(result);
		}
		return result;
	}
	
	/**
	 * @param bits
	 * @return	Empty BitSet2 if bits does not contain the Start flag.
	 * 			Data bits after the Start flag if it does contain the start flag.
	 */
	private BitSet2 getDataAfterStartFlag(BitSet2 bits){
		if(!receivedStartFlag){
			BitSet2 junk = BitSet2.concatenate(lastJunk, bits);
			int contains = junk.contains(FLAG_START_OF_FRAME.getFlag());
			if(contains>0){
				receivedStartFlag = true;
				return bits.get(contains+FLAG_START_OF_FRAME.getFlag().length(), bits.length());
			}
			lastJunk = junk.get(junk.length()-FLAG_START_OF_FRAME.getFlag().length(), junk.length());
			
			return new BitSet2();
		}else{
			return new BitSet2(bits);
		}
	}
	/**
	 * @param bits
	 * @return	Empty BitSet2 if bits does not contain the End flag.
	 * 			Junk bits after the End flag if it does contain the end flag.
	 */
	private BitSet2 getDataAfterEndFlag(BitSet2 bits){
		int contains = bits.contains(FLAG_END_OF_FRAME.getFlag());
		if(contains>0){
			return bits.get(contains + FLAG_END_OF_FRAME.getFlag().length(), bits.length());
		}
		return new BitSet2();
	}
	private BitSet2 getDataBeforeEndFlag(BitSet2 bits){
		int contains = bits.contains(FLAG_END_OF_FRAME.getFlag());
		if(contains>0){
			return bits.get(0, contains);
		}
		return (BitSet2) bits.clone();
	}
	
	
	

	@Override
	public BitSet2 getConverted() {
		return placeFlags(stuff(stored)); 
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public boolean isFull() {
		return stored.length()==dataBitCount || childNodes[0].isFull() || isFull;
	}

	@Override
	public boolean isReady() {
		return childNodes[0].isReady();
	}

	@Override
	public boolean isCorrect() {
		return childNodes[0].isCorrect();
	}

	
	@Override
	public boolean isFiller() {
		return stored.length()==0 || (childNodes[0] instanceof Node.Fillable && ((Node.Fillable)childNodes[0]).isFiller());
	}

	@Override
	public Node[] getChildNodes() {
		return childNodes;
	}

	@Override
	public Node getClone() {
		return new FlaggingNode(parent, childNodes[0].getClone(), dataBitCount);
	}

	@Override
	public String getStateString() {
		if(!isFull()){
			if(isFiller()){
				return "Filler";
			}else{
				return "Filling";
			}
		}else if(isFull() && getOriginal().length()==stored.length()){
			return "Filled, ready to send";
		}else if(isFull() && getOriginal().length()!=stored.length()){
			return "Filled, ready be read";
		}
		
		return "Unkown";
	}

}
