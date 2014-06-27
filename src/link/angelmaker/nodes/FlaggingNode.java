package link.angelmaker.nodes;

import java.util.Arrays;

import link.angelmaker.AngelMaker;
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
	public static final Flag FLAG_START_OF_FRAME = new BasicFlag(new BitSet2(
			"10011001"));
	public static final Flag FLAG_END_OF_FRAME = new BasicFlag(new BitSet2(
			"00111001101"));
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

	private boolean receivedStartFlag = false;
	private boolean isFull;
	private BitSet2 lastReceivedConvertedJunk;
	
	public static int maxBitsExpected = SequencedNode.PACKET_BIT_COUNT;
	
	public FlaggingNode(Node parent) {
		this.dataBitCount = maxBitsExpected;
		children = new Node.Resetable[] { new ErrorDetectionNode(this, dataBitCount) };
		
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
	}
	
	public FlaggingNode(Node parent, Node.Resetable child, int dataBitCount) {
		children = new Node.Resetable[] { child };
		this.dataBitCount = dataBitCount;
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
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
		//TODO optimize
		if (!receivedStartFlag) {
			BitSet2 tempConcat = BitSet2.concatenate(lastReceivedConvertedJunk,
					bits); // Add just received to already received.
			// Keep only just received + max flag length (so it cannot
			// Infinitely grow.
			lastReceivedConvertedJunk = tempConcat.get(
					Math.max(0,
							tempConcat.length()
									- FLAG_START_OF_FRAME.getFlag().length()
									- bits.length()), tempConcat.length());
			BitSet2 afterStart = getDataAfterStartFlag(lastReceivedConvertedJunk);
			//System.out.println("Data after start:"+afterStart);
			if (afterStart.length() >= 0) {
				storedConverted = afterStart;
			}
		} else {
			storedConverted = BitSet2.concatenate(storedConverted, bits);
		}
		int contains = getRealEndFlagIndex(storedConverted);
		if (receivedStartFlag && contains >= 0) {
			// Received start and end flag.
			isFull = true;
			stored = unStuff(getDataBeforeEndFlag(storedConverted));
			children[0].giveConverted(stored);
			//System.out.println("GiveConverted done");
			return storedConverted.get(contains
					+ FLAG_END_OF_FRAME.getFlag().length(),
					storedConverted.length());
		} else {
			// Does not have end flag yet.
			//System.out.println("GiveConverted done");
			return new BitSet2();
		}
	}

	@Override
	public BitSet2 getConverted() {
		return placeFlags(stuff(children[0].getConverted()));
	}

	private int getRealEndFlagIndex(BitSet2 bits) {
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
	 * @bits BitSet2 to look in.
	 * @return the data after the start flag, empty bitset2 if bits does not
	 *         contain the start flag
	 */
	private BitSet2 getDataAfterStartFlag(BitSet2 bits) {
		int contains = bits.contains(FLAG_START_OF_FRAME.getFlag());
		if (contains >= 0) {
			receivedStartFlag = true;
			//System.out.println("Seen start flag");
			return bits.get(contains + FLAG_START_OF_FRAME.getFlag().length(),
					bits.length());
		} else {
			return new BitSet2();
		}
	}

	private BitSet2 getDataBeforeEndFlag(BitSet2 bits) {
		int contains = getRealEndFlagIndex(bits);
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
		return getOriginal().length()==0;
	}

	@Override
	public Node getClone() {
		return new FlaggingNode(parent, (Node.Resetable)children[0].getClone(), dataBitCount);
	}
	
	@Override
	public Node getFiller(){
		return AngelMaker.TOP_NODE_IN_USE.getClone();
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
	}

}