package link.angelmaker.nodes;

import link.angelmaker.AngelMaker;
import link.angelmaker.manager.ConstantRetransmittingManager;
import util.BitSet2;

/**
 * Accepts one-time injection of data.
 * Sequence numbers / flags can be set afterwards.
 * @author I3anaan
 *
 */
public class SequencedNode implements Node, Node.Internal {

	private Node parent;
	private int maxDataSize;
	private int messageBitCount;
	private boolean full;
	private BitSet2 storedData;
	private BitSet2 sequenceNumber;
	private BitSet2 message;
	
	public static final int PACKET_BIT_COUNT = 64;
	public static final int MESSAGE_BIT_COUNT = 4;
	
	public SequencedNode(Node parent, int maxDataSize, int messageBitCount){
		this.parent = parent;
		this.maxDataSize = maxDataSize;
		this.messageBitCount = messageBitCount;
		this.storedData = new BitSet2();
	}
	
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		this.storedData = bits.get(0,Math.min(bits.length(), maxDataSize));
		full = true;
		return bits.get(Math.min(bits.length(), maxDataSize),bits.length());
	}

	@Override
	public BitSet2 getOriginal() {
		return storedData;
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		BitSet2 bitsToUse = bits.get(0,Math.min(bits.length(),maxDataSize+2*messageBitCount));
		sequenceNumber = bitsToUse.get(0,messageBitCount);
		message = bitsToUse.get(bits.length()-messageBitCount,bits.length());
		storedData = bitsToUse.get(messageBitCount, bits.length()-messageBitCount);
		full = true;
		return bits.get(Math.min(bits.length(),maxDataSize+2*messageBitCount),bits.length());
	}

	@Override
	public BitSet2 getConverted() {
		return BitSet2.concatenate(sequenceNumber, BitSet2.concatenate(storedData, message));
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public boolean isFull() {
		return full;
	}

	@Override
	public boolean isReady() {
		return isFull();
	}

	@Override
	public boolean isCorrect() {
		return true;
	}

	@Override
	public Node getClone() {
		Node clone = new SequencedNode(parent, maxDataSize, messageBitCount);
		if(isFull()){
			clone.giveConverted(getConverted());
		}
		return clone;
	}

	@Override
	public Node[] getChildNodes() {
		return null;
	}

	@Override
	public String getStateString() {
		return isFull() ? "Full" : "Empty";
	}
	
	public int getSeq(){
		System.out.println("SequenceNumber = "+sequenceNumber);
		return sequenceNumber.getUnsignedValue();
	}
	
	public int getMessage(){
		return message.getUnsignedValue();
	}
	
	public void setSeq(BitSet2 seq){
		if(seq.length()==MESSAGE_BIT_COUNT){
			this.sequenceNumber = seq;
		}else{
			AngelMaker.logger.error("Trying to set packet seq number with too many bits");
		}
	}
	
	public void setMessage(BitSet2 msg){
		if(msg.length()==MESSAGE_BIT_COUNT){
			this.message = msg;
		}else{
			AngelMaker.logger.error("Trying to set packet msg with too many bits");
		}
	}
	
	
	@Override
	public String toString(){
		return "SequencedNode";
	}

}
