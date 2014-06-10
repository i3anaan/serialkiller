package link.angelmaker.nodes;

import java.util.Arrays;

import common.Graph;

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
public class FlaggingNode implements Node, Node.Internal, Node.Fillable {

	private Node[] childNodes;
	private Node parent;
	public static final Flag FLAG_START_OF_FRAME = new BasicFlag(new BitSet2(
			"10011001"));
	public static final Flag FLAG_END_OF_FRAME = new BasicFlag(new BitSet2(
			"00111001101"));
	/*
	 * Requirements for flags: FLAG_END_OF_FRAME Does NOT contain part of itself
	 * starting at the right. In other words: It should not be possible to
	 * append a X amount of bits left of the FLAG_END_OF_FRAME resulting in that
	 * new sequence to contain a FLAG_END_OF_FRAME before the real
	 * FLAG_END_OF_FRAME starts.
	 * 
	 */

	// Original, unstuffed, unflagged data.
	private BitSet2 stored;
	private BitSet2 storedConverted;
	private int dataBitCount;

	private boolean receivedStartFlag = false;
	private boolean isFull;
	private BitSet2 lastJunk;
	private BitSet2 lastReceivedConvertedJunk;

	private int graph = 0; // TODO this is DEBUG

	public FlaggingNode(Node parent, int dataBitCount) {
		// childNodes = new Node[]{new FrameCeptionNode<Node>(parent, 1)};
		childNodes = new Node[] { new PureNode(parent, dataBitCount) };
		this.dataBitCount = dataBitCount;
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastJunk = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
	}

	public FlaggingNode(Node parent, Node child, int dataBitCount) {
		childNodes = new Node[] { child };
		this.dataBitCount = dataBitCount;
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastJunk = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
	}

	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		System.out.println("FlaggingNode, original received: " + bits);
		int i;
		for (i = 0; i < bits.length() && stored.length() < dataBitCount; i++) {
			stored.addAtEnd(bits.get(i));
		}
		if (stored.length() >= dataBitCount) {
			isFull = true;
		}
		System.out.println("FlaggingNode,giveOriginal, stored = " + stored);
		if (isFull) {
			System.out.println("Collected Full converted string = "
					+ getConverted());
			BitSet2 remaining = childNodes[0].giveOriginal(stored);
			System.out.println("Remaining data after giving to childNodes = "
					+ remaining);
			System.out.println("Childs currently have: "
					+ childNodes[0].getOriginal());
			System.out.println("Making graph " + graph);
			//Graph.makeImage(Graph.getFullGraphForNode(this, true),
			//		"FlaggingNodePutOriginal" + graph);
			graph++;
			if (remaining.length() > 0) {
				AngelMaker.logger.alert("FlaggingNode is spilling data");
			}
		}
		System.out.println("Original Stored in childNodes: " + getOriginal());

		return bits.get(i, bits.length());
	}

	@Override
	public BitSet2 getOriginal() {
		return childNodes[0].getOriginal();
	}

	/**
	 * Receive 7 bits extra after spotting the first end of frame flag. Check if
	 * there is no en of frame flag in these 7 bits. Take last possible end of
	 * frame start. Return unused stuff (even from previous calls) afterwards.
	 * This for situation: 1111011001100110 DDDDDDDDFFFFFFFF Correct
	 * DDDDFFFFFFFF---- (possibly) Read
	 */
	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		System.out.println("FlaggingNode got Converted: " + bits);
		if (!receivedStartFlag) {
			BitSet2 tempConcat = BitSet2.concatenate(lastReceivedConvertedJunk,
					bits); // Add just received to already received.
			// Keep only just received + max flag length (so it cannot
			// infinetely grow.
			lastReceivedConvertedJunk = tempConcat.get(
					Math.max(0,
							tempConcat.length()
									- FLAG_START_OF_FRAME.getFlag().length()
									- bits.length()), tempConcat.length());
			System.out.println("LastReceivedConvertedJunk = "
					+ lastReceivedConvertedJunk);
			BitSet2 afterStart = getDataAfterStartFlag(lastReceivedConvertedJunk);
			if (afterStart.length() >= 0) {
				storedConverted = afterStart;
			}
		} else {
			storedConverted = BitSet2.concatenate(storedConverted, bits);
		}
		System.out.println("StoredConverted = " + storedConverted);
		int contains = getRealEndFlagIndex(storedConverted);
		if (receivedStartFlag
				&& contains >= 0
				&& contains + 2 * FLAG_END_OF_FRAME.getFlag().length() - 2 >= storedConverted
						.length()) {
			// Received start and end flag.

			System.out.println("FlaggingNode, received start and end flag");
			isFull = true;
			stored = new BitSet2();
			System.out.println("Converted, no flags: "
					+ getDataBeforeEndFlag(storedConverted));
			System.out.println("Converted, no flags, unstuffed: "
					+ unStuff(getDataBeforeEndFlag(storedConverted)));
			giveOriginal(unStuff(getDataBeforeEndFlag(storedConverted)));

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
		return placeFlags(stuff(getOriginal()));
	}

	private int getRealEndFlagIndex(BitSet2 bits) {
		int contains = bits.contains(FLAG_END_OF_FRAME.getFlag());
		// System.out.println(bits+"  Contains before endFlagEnsurer: "+contains);
		int contains2 = bits.contains(FLAG_END_OF_FRAME.getFlag(), contains);
		int containsUsed = contains;
		// System.out.println("contains2 = "+contains2);
		while (contains2 >= 0
				&& contains2 < contains + FLAG_END_OF_FRAME.getFlag().length()) {
			containsUsed = contains2;
			contains2 = bits.contains(FLAG_END_OF_FRAME.getFlag(),
					contains2 + 1);
			// System.out.println("contains2 = "+contains2);
		}
		// System.out.println("Contains after endFlagEnsurer: "+containsUsed);
		return containsUsed;
	}

	private BitSet2 placeFlags(BitSet2 bits) {
		return BitSet2.concatenate(
				BitSet2.concatenate(FLAG_START_OF_FRAME.getFlag(), bits),
				FLAG_END_OF_FRAME.getFlag());

	}

	private BitSet2 removeFlags(BitSet2 bits) {
		return getDataBeforeEndFlag(getDataAfterStartFlag(bits));
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
		System.out.println("GetDataAfterStartFlag, contains: " + contains);
		if (contains >= 0) {
			receivedStartFlag = true;
			System.out.println("GetDataAfterStartFlagReturns: "
					+ bits.get(contains
							+ FLAG_START_OF_FRAME.getFlag().length(),
							bits.length()));// TODO
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
	
	public Node getExtraStorageNode(int bitCount){
		return new PureNode(this,bitCount);
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public boolean isFull() {
		return stored.length() == dataBitCount || childNodes[0].isFull()
				|| isFull;
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
		return stored.length() == 0
				|| (childNodes[0] instanceof Node.Fillable && ((Node.Fillable) childNodes[0])
						.isFiller());
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
		return "FlaggingNode[" + Arrays.toString(childNodes) + "]";
	}

}
