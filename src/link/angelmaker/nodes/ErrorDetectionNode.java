package link.angelmaker.nodes;

import com.google.common.base.Optional;

import link.angelmaker.codec.Codec;
import link.angelmaker.codec.ParityBitsCodec;
import util.BitSet2;
import util.EmptyBitSet2;

public class ErrorDetectionNode extends AbstractNode implements Node.Resetable,Node.OneTimeInjection {
	private boolean full;
	private boolean correct;
	private int maxDataSize;
	private Node.Resetable child;
	private Codec codec;
	
	public ErrorDetectionNode(Node parent, int maxDataSize, Codec codec){
		this.parent = parent;
		this.maxDataSize = maxDataSize;
		this.codec = codec;
		child = new SequencedNode(this,SequencedNode.PACKET_BIT_COUNT,SequencedNode.MESSAGE_BIT_COUNT);
	}
	
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		if(!full){
			child.giveOriginal(bits);
			full = true;
			correct = true;
			return bits.get(Math.min(maxDataSize,bits.length()), bits.length());
		}
		return EmptyBitSet2.getInstance();
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
			Optional<BitSet2> decoded = codec.decode(bits);
			correct = decoded.isPresent();
			if(correct){
				child.giveConverted(decoded.get());
			}
			return new BitSet2();
		}catch(IllegalArgumentException e){
			correct = false;
		}
		return EmptyBitSet2.getInstance();
	}

	@Override
	public BitSet2 getConverted() {
		return codec.encode(child.getConverted());
	}

	@Override
	public boolean isFull() {
		return full;
	}

	@Override
	public boolean isCorrect() {
		return correct;
	}

	@Override
	public Node getClone() {
		Node clone = new ErrorDetectionNode(parent, maxDataSize,codec);
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


	@Override
	public void reset() {
		child.reset();
		full = false;
		correct = true;		
	}

}
