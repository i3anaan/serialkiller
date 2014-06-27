package link.angelmaker.nodes;

import com.google.common.base.Optional;

import link.angelmaker.AngelMaker;
import link.angelmaker.codec.ParityBitsCodec;
import util.BitSet2;

public class ErrorDetectionNode implements Node,Node.Internal {

	private Node parent;
	private boolean full;
	private boolean correct;
	private int maxDataSize;
	private Node child;
	
	public ErrorDetectionNode(Node parent, int maxDataSize){
		this.parent = parent;
		this.maxDataSize = maxDataSize;
		child = new SequencedNode(this,SequencedNode.PACKET_BIT_COUNT,SequencedNode.MESSAGE_BIT_COUNT);
	}
	
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		if(!full){
			child.giveOriginal(bits.get(0, Math.min(maxDataSize,bits.length())));
			full = true;
			correct = true;
			return bits.get(Math.min(maxDataSize,bits.length()), bits.length());
		}
		return bits;
	}

	@Override
	public BitSet2 getOriginal() {
		return child.getOriginal();
	}

	/**
	 * Only accepts a one-time injection.
	 */
	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		try{
			
			int maxExpectedBits = (SequencedNode.PACKET_BIT_COUNT + SequencedNode.MESSAGE_BIT_COUNT*2)*ParityBitsCodec.ENCODED_BYTE/8;
			BitSet2 bitsToUse = bits.get(0,Math.min(bits.length(),maxExpectedBits));
			Optional<BitSet2> decoded = ParityBitsCodec.decode(bitsToUse);
			correct = decoded.isPresent();
			if(correct){
				child.giveConverted(decoded.get());
			}
			return bits.get(Math.min(bits.length(),maxExpectedBits),bits.length());
		}catch(IllegalArgumentException e){
			correct = false;
		}
		return new BitSet2();
	}

	@Override
	public BitSet2 getConverted() {
		return ParityBitsCodec.encode(child.getConverted());
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
		return correct;
	}

	@Override
	public Node getClone() {
		Node clone = new ErrorDetectionNode(parent, maxDataSize);
		if(isFull()){
			clone.giveConverted(getConverted());
		}
		return clone;
	}

	@Override
	public Node[] getChildNodes() {
		return new Node[]{child};
	}

	@Override
	public String getStateString() {
		return (isFull() ? (isCorrect() ? "Correct" : "Incorrect") : "Empty");
	}
	
	@Override
	public String toString(){
		return "ErrorDetectionNode(Parity)["+child+"]";
	}

}
