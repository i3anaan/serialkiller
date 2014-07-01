package link.angelmaker.nodes;

import link.angelmaker.AngelMaker;
import link.angelmaker.AngelMakerConfig;
import util.BitSet2;
import util.EmptyBitSet2;

/**
 * Accepts one-time injection of data.
 * Sequence numbers / flags can be set afterwards.
 * @author I3anaan
 *
 */
public class SequencedNode extends AbstractNode implements Node.OneTimeInjection, Node.Resetable,Node.Fillable{
	private int maxDataSize;
	private int messageBitCount;
	private boolean full;
	private BitSet2 storedData;
	private BitSet2 sequenceNumber;
	private BitSet2 message;
	
	public SequencedNode(Node parent, int maxDataSize, int messageBitCount){
		this.parent = parent;
		this.maxDataSize = maxDataSize;
		this.messageBitCount = messageBitCount;
		this.storedData = new BitSet2();
		this.sequenceNumber = new BitSet2();
		this.message = new BitSet2();
	}
	
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		this.storedData = bits.get(0,Math.min(bits.length(), maxDataSize));
		full = true;
		return EmptyBitSet2.getInstance();
	}

	@Override
	public BitSet2 getOriginal() {
		return storedData;
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		sequenceNumber = bits.get(0,Math.min(messageBitCount,bits.length()));
		message = bits.get(Math.min(messageBitCount,bits.length()),Math.min(messageBitCount*2,bits.length()));
		storedData = bits.get(Math.min(messageBitCount*2,bits.length()), bits.length());
		full = true;
		return EmptyBitSet2.getInstance();
	}

	@Override
	public BitSet2 getConverted() {
		return BitSet2.concatenate(sequenceNumber, BitSet2.concatenate(message,storedData));
	}

	@Override
	public boolean isFull() {
		return full;
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
	public String getStateString() {
		return isFull() ? "Full" : "Empty";
	}
	
	public BitSet2 getSeq(){
		return sequenceNumber;
	}
	
	public BitSet2 getMessage(){
		return message;
	}
	
	public void setSeq(BitSet2 seq){
		if(seq.length()==AngelMakerConfig.MESSAGE_BIT_COUNT){
			this.sequenceNumber = seq;
		}else{
			AngelMaker.logger.error("Trying to set packet seq number with too many bits");
		}
	}
	
	public void setMessage(BitSet2 msg){
		if(msg.length()==AngelMakerConfig.MESSAGE_BIT_COUNT){
			this.message = msg;
		}else{
			AngelMaker.logger.error("Trying to set packet msg with too many bits");
		}
	}
	
	
	@Override
	public String toString(){
		return "SequencedNode";
	}


	@Override
	public void reset() {
		this.storedData.clear();
		full = false;
	}


	@Override
	public boolean isFiller() {
		return storedData.length()==0;
	}
	
	@Override
	public boolean isCorrect(){
		return message.length()==AngelMakerConfig.MESSAGE_BIT_COUNT && sequenceNumber.length()==AngelMakerConfig.MESSAGE_BIT_COUNT;
	}


	@Override
	public Node getFiller() {
		return new SequencedNode(null, AngelMakerConfig.PACKET_BIT_COUNT, AngelMakerConfig.MESSAGE_BIT_COUNT);
	}

}
