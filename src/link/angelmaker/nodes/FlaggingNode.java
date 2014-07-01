package link.angelmaker.nodes;

import java.util.Arrays;

import link.angelmaker.AngelMaker;
import link.angelmaker.AngelMakerConfig;
import link.angelmaker.flags.Flag;
import util.BitSet2;
import util.EmptyBitSet2;

/**
 * A Node that flags its data, has 1 childNode. It reads a bit stream, consumes
 * till it spots a start of frame flag. Then start reading data after that flag,
 * till it spots an end of frame flag. The data in between is handed down, to the next node.
 * giveOriginal() is a one time injection
 * giveConverted() is not.
 * 
 * @author I3anaan
 * 
 */
public class FlaggingNode extends AbstractNode implements Node.Fillable,Node.Resetable {	
	public Flag FLAG_START_OF_FRAME;
	public Flag FLAG_END_OF_FRAME;
	protected Node.Resetable[] children; //children this Node has.
	private BitSet2 stored;	//The original data stored in this Node.
	private BitSet2 storedConverted;	//Data collected so far through giveConverted(). 
	
	/**
	 * (possible) junk received through giveConverted().
	 * This is used till the startflag is found.
	 */
	private BitSet2 lastReceivedConvertedJunk; 
	private int checkedForStartFlagInJunkTill = 0;//Value to optimize contains function.
	
	private boolean receivedStartFlag = false;
	private boolean isFull;
	
	public FlaggingNode(Node parent) {
		setFlags();
		children = new Node.Resetable[] { new ErrorDetectionNode(this) };
		
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
		if(!isSolidEndFlag(FLAG_END_OF_FRAME.getFlag())){
			AngelMaker.logger.bbq("Invalid End of frame flag, ealier occurence possible");
		}
	}
	
	public FlaggingNode(Node parent, Node.Resetable child) {
		setFlags();
		children = new Node.Resetable[] { child };
		this.parent = parent;
		stored = new BitSet2();
		storedConverted = new BitSet2();
		lastReceivedConvertedJunk = new BitSet2();
		if(!isSolidEndFlag(FLAG_END_OF_FRAME.getFlag())){
			AngelMaker.logger.bbq("Invalid End of frame flag, ealier occurence possible");
		}
	}
	
	private void setFlags(){
		FLAG_START_OF_FRAME = AngelMakerConfig.getStartFlag();
		FLAG_END_OF_FRAME = AngelMakerConfig.getEndFlag();
	}
	
	/**
	 * Used to set the parent after constructing.
	 * This was used to better draw diagrams.
	 * @param parent
	 */
	public void setParent(Node parent){
		this.parent = parent;
	}
	
	@Override
	/**
	 * Give original data.
	 * Accepts one time injection.
	 */
	public BitSet2 giveOriginal(BitSet2 bits) {
		int i;
		for (i = 0; i < bits.length(); i++) {
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
	/**
	 * Give a converted data stream, the FlaggingNode will consume these bits till it finds the start flag.
	 * After which it stores the bits till it finds the end flag, then it considers the data inbetween to be complete.
	 * It then unstuffs the data and gives it to its child.
	 */
	public BitSet2 giveConverted(BitSet2 bits) {
		if (!receivedStartFlag) {
			//Look for start flag
			lastReceivedConvertedJunk.addAtEnd(bits);
			
			BitSet2 afterStart = getDataAfterStartFlag();
			if (afterStart.length()>0) {
				storedConverted = afterStart;
			}
		} else {
			//Start flag found, append at data received
			storedConverted.addAtEnd(bits);
		}
		
		int contains = getEndFlagIndex(storedConverted);
		//Check if data received so far contains endflag.
		if (receivedStartFlag && contains >= 0) {
			// Received start and end flag.
			isFull = true;
			stored = unStuff(getDataBeforeEndFlag(storedConverted));
			children[0].giveConverted(stored);
			
			return storedConverted.get(contains
					+ FLAG_END_OF_FRAME.getFlag().length(),
					storedConverted.length());
		} else {
			// Does not have end flag yet.
			return EmptyBitSet2.getInstance();
		}
	}

	@Override
	public BitSet2 getConverted() {
		return placeFlags(stuff(children[0].getConverted()));
	}
	private int getEndFlagIndex(BitSet2 bits) {
		return bits.contains(FLAG_END_OF_FRAME.getFlag());
	}

	/**
	 * Places the flags around bits.
	 * Does NOT stuff the bits first.
	 */
	private BitSet2 placeFlags(BitSet2 bits) {
		return BitSet2.concatenate(
				BitSet2.concatenate(FLAG_START_OF_FRAME.getFlag(), bits),
				FLAG_END_OF_FRAME.getFlag());

	}

	/**
	 * Stuffs the bits given, removing occurrences of the flags it stuffs.
	 * @param bits to stuff
	 * @return	Stuffed data, not containing any flags.
	 */
	private BitSet2 stuff(BitSet2 bits) {
		BitSet2 result = (BitSet2) bits.clone();
		Flag[] flags = new Flag[] { FLAG_START_OF_FRAME, FLAG_END_OF_FRAME };
		for (int i = 0; i < flags.length; i++) {
			flags[i].stuff(result);
		}
		return result;
	}

	/**
	 * Reverses the stuffing.
	 */
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
	 * @return the data after the start flag, empty if lastReceivedConvertedJunk does not
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
			return EmptyBitSet2.getInstance();
		}
	}

	/**
	 * @param bits to look in
	 * @return	All the bits before the first occurence of the end flag.
	 * 			Returns a copy of the bits if it does not contain the end flag.
	 */
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
		return new FlaggingNode(parent, (Node.Resetable)children[0].getClone());
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