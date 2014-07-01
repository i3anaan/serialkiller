package link.angelmaker.nodes;

import java.util.Arrays;

import link.angelmaker.AngelMaker;
import link.angelmaker.codec.Codec;
import link.angelmaker.codec.HammingCodec;
import link.angelmaker.codec.MixedCodec;
import link.angelmaker.codec.NaiveRepeaterCodec;
import link.angelmaker.codec.ParityBitsCodec;
import link.angelmaker.flags.DummyFlag;
import link.angelmaker.flags.FixedEndFlag;
import link.angelmaker.flags.Flag;
import util.BitSet2;

/**
 * A Node that flags its data, has 1 childNode. It reads a bit stream, consumes
 * till it spots a start of frame flag. Then start reading data after that flag,
 * till it spots an end of frame flag. It collects bits till it is full (got
 * dataBitCount bits). Only when it detects its full, it gives the data to its
 * childNode. When not full, getOriginal() returns an empty BitSet2.
 * 
 * Best to use either giveConverted or giveOriginal, not both.
 * 
 * @author I3anaan
 * 
 */
public class FlaggingNode extends AbstractNode implements Node.Fillable, Node.OneTimeInjection,Node.Resetable {	
	public Flag FLAG_START_OF_FRAME;
	public Flag FLAG_END_OF_FRAME;
	protected Node.Resetable[] children;
	
	
	/*
	 * Requirements for flags: FLAG_END_OF_FRAME Does NOT contain part of itself
	 * starting at the right. In other words: It should not be possible to
	 * append a X amount of bits left of the FLAG_END_OF_FRAME resulting in that
	 * new sequence to contain a FLAG_END_OF_FRAME before the real
	 * FLAG_END_OF_FRAME starts.
	 */

	// Original, unstuffed, unflagged data.
	private BitSet2 stored;
	private BitSet2 storedConverted;
	private int dataBitCount;
	
	private int checkedForStartFlagInJunkTill = 0;
	private boolean receivedStartFlag = false;
	private boolean isFull;
	private BitSet2 lastReceivedConvertedJunk;
	
	private static final Codec CODEC = new MixedCodec(new Codec[]{new HammingCodec(8),new ParityBitsCodec()});
	
	
	public static int maxBitsExpected = SequencedNode.PACKET_BIT_COUNT;
	
	
	public FlaggingNode(Node parent) {
		setFlags();
		this.dataBitCount = maxBitsExpected;
		children = new Node.Resetable[] { new ErrorDetectionNode(this, dataBitCount,CODEC) };
		
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
		if(!isSolidEndFlag(FLAG_END_OF_FRAME.getFlag())){
			AngelMaker.logger.bbq("Invalid End of frame flag, ealier occurence possible");
		}
	}
	
	public FlaggingNode(Node parent, Node.Resetable child, int dataBitCount) {
		setFlags();
		children = new Node.Resetable[] { child };
		this.dataBitCount = dataBitCount;
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
		if(!isSolidEndFlag(FLAG_END_OF_FRAME.getFlag())){
			AngelMaker.logger.bbq("Invalid End of frame flag, ealier occurence possible");
		}
	}
	
	private void setFlags(){
		FLAG_START_OF_FRAME = new DummyFlag(new BitSet2("11101"));
		FLAG_END_OF_FRAME = new FixedEndFlag();
		//FLAG_START_OF_FRAME = new BasicFlag(new BitSet2("10011001"));
		//FLAG_END_OF_FRAME = new BasicFlag(new BitSet2("00111001101"));
	}
	
	
	public void setParent(Node parent){
		this.parent = parent;
	}
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		int i;
		for (i = 0; i < bits.length() && stored.length() < dataBitCount; i++) {
			stored.addAtEnd(bits.get(i));
		}
		isFull = true;
		if (isFull) {
			BitSet2 remaining = children[0].giveOriginal(stored);
			if (remaining.length() > 0) {
				AngelMaker.logger.alert("FlaggingNode is spilling data");
			}
		}
		return bits.get(i, bits.length());
	}

	@Override
	public BitSet2 getOriginal() {
		return children[0].getOriginal();
	}
	
	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		//System.out.println("Give Converted: "+lastReceivedConvertedJunk+"\t storedConverted: "+storedConverted);
		if (!receivedStartFlag) {
			//This code does less allocation and is easier on CPU, can however (theoretically) store infinitely long bitsets.
			//TODO better?
			lastReceivedConvertedJunk.addAtEnd(bits);
			
			BitSet2 afterStart = getDataAfterStartFlag();
			if (afterStart!=null) {
				storedConverted = afterStart;
			}
		} else {
			storedConverted.addAtEnd(bits);
		}
		
		int contains = getEndFlagIndex(storedConverted);
		if (receivedStartFlag && contains >= 0) {
			// Received start and end flag.
			isFull = true;
			stored = unStuff(getDataBeforeEndFlag(storedConverted));
			//System.out.println("Full data: "+stored);
			children[0].giveConverted(stored);
			
			return storedConverted.get(contains
					+ FLAG_END_OF_FRAME.getFlag().length(),
					storedConverted.length());
		} else {
			// Does not have end flag yet.
			return new BitSet2();
		}
	}

	@Override
	public BitSet2 getConverted() {
		return placeFlags(stuff(children[0].getConverted()));
	}

	private int getEndFlagIndex(BitSet2 bits) {
		return bits.contains(FLAG_END_OF_FRAME.getFlag());
	}

	private BitSet2 placeFlags(BitSet2 bits) {
		return BitSet2.concatenate(
				BitSet2.concatenate(FLAG_START_OF_FRAME.getFlag(), bits),
				FLAG_END_OF_FRAME.getFlag());

	}

	private BitSet2 stuff(BitSet2 bits) {
		BitSet2 result = (BitSet2) bits.clone();
		Flag[] flags = new Flag[] { FLAG_START_OF_FRAME, FLAG_END_OF_FRAME };
		for (int i = 0; i < flags.length; i++) {
			flags[i].stuff(result);
		}
		return result;
	}

	private BitSet2 unStuff(BitSet2 bits) {
		BitSet2 result = (BitSet2) bits.clone();
		Flag[] flags = new Flag[] { FLAG_START_OF_FRAME, FLAG_END_OF_FRAME };
		for (int i = flags.length - 1; i >= 0; i--) {
			flags[i].unStuff(result);
		}
		return result;
	}

	/**
	 * Looks in lastReceivedConvertedJunk for a start flag.
	 * @return the data after the start flag, null if bits does not
	 *         contain the start flag
	 */
	private BitSet2 getDataAfterStartFlag() {
		int contains = lastReceivedConvertedJunk.contains(FLAG_START_OF_FRAME.getFlag(), checkedForStartFlagInJunkTill);
		checkedForStartFlagInJunkTill = Math.max(lastReceivedConvertedJunk.length()-FLAG_START_OF_FRAME.getFlag().length()+1,0);
		if (contains >= 0) {
			receivedStartFlag = true;
			return lastReceivedConvertedJunk.get(contains + FLAG_START_OF_FRAME.getFlag().length(),
					lastReceivedConvertedJunk.length());
		} else {
			return null;
		}
	}

	private BitSet2 getDataBeforeEndFlag(BitSet2 bits) {
		int contains = getEndFlagIndex(bits);
		if (contains >= 0) {
			return bits.get(0, contains);
		} else {
			return (BitSet2) bits.clone();
		}
	}

	@Override
	public Node[] getChildNodes(){
		return children;
	}
	@Override
	public boolean isFull() {
		return isFull;
	}

	@Override
	public boolean isReady() {
		return children[0].isReady() || stored.length()==0;
	}

	@Override
	public boolean isCorrect() {
		return children[0].isCorrect();
	}

	@Override
	public boolean isFiller() {
		return isFull() && getOriginal().length()==0;
	}

	@Override
	public Node getClone() {
		return new FlaggingNode(parent, (Node.Resetable)children[0].getClone(), dataBitCount);
	}
	
	@Override
	public Node getFiller(){
		Node.Resetable clone =(Node.Resetable)this.getClone();
		clone.reset();
		return clone;
	}

	@Override
	public String getStateString() {
		if (!isFull()) {
			if (isFiller()) {
				return "Filler";
			} else {
				return "Filling";
			}
		} else if (isFull() && getOriginal().length() == stored.length()) {
			return "Filled, ready to send";
		} else if (isFull() && getOriginal().length() != stored.length()) {
			return "Filled, ready be read";
		}

		return "Unkown";
	}

	@Override
	public String toString() {
		return "FlaggingNode[" + Arrays.toString(children) + "]";
	}

	@Override
	public void reset() {
		for(Node.Resetable n : children){
			n.reset();
		}
		stored.clear();
		storedConverted.clear();
		receivedStartFlag = false;
		isFull = false;
		lastReceivedConvertedJunk.clear();
		checkedForStartFlagInJunkTill=0;
	}
	
	/**
	 * @return true if no sequence of bits can be placed in front of the end flag to create an extra new endflag earlier then the real endflag.
	 */
	public static boolean isSolidEndFlag(BitSet2 flag){
		for(int i=1;i<flag.length();i++){
			if(flag.get(0,i).equals(flag.get(flag.length()-i,flag.length()))){
				return false;
			}
		}
		
		return true;
	}

}