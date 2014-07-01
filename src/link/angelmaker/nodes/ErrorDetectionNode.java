package link.angelmaker.nodes;

import com.google.common.base.Optional;

import link.angelmaker.AngelMakerConfig;
import util.BitSet2;
import util.EmptyBitSet2;

public class ErrorDetectionNode extends AbstractNode implements Node.Resetable,Node.OneTimeInjection {
	private boolean full;
	private boolean correct;
	private Node.Resetable child;
	
	public ErrorDetectionNode(Node parent){
		this.parent = parent;
		child = new SequencedNode(this,AngelMakerConfig.PACKET_BIT_COUNT,AngelMakerConfig.MESSAGE_BIT_COUNT);
	}
	
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		if(!full){
			child.giveOriginal(bits);
			full = true;
			correct = true;
			return EmptyBitSet2.getInstance();
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
			Optional<BitSet2> decoded = AngelMakerConfig.CODEC.decode(bits);
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
		return AngelMakerConfig.CODEC.encode(child.getConverted());
	}

	@Override
	public boolean isFull() {
		return full;
	}

	@Override
	public boolean isCorrect() {
		return correct && child.isCorrect();
	}

	@Override
	public Node getClone() {
		Node clone = new ErrorDetectionNode(parent);
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
		return "ErrorDetectionNode("+AngelMakerConfig.CODEC.getClass().getSimpleName()+")["+child.getClass().getSimpleName()+"]";
	}


	@Override
	public void reset() {
		child.reset();
		full = false;
		correct = true;		
	}

}
