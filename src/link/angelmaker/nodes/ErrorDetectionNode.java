package link.angelmaker.nodes;

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
		child = new SequencedNode();
	}
	
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		if(!full){
			child.giveOriginal(bits.get(0, Math.min(maxDataSize,bits.length())));
			full = true;
			return bits.get(Math.min(maxDataSize,bits.length()), bits.length());
		}
		return new BitSet2();
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
			BitSet2 decoded = codec.ParityBitsCodec.decode(bits);
			correct = decoded!=null;
			if(correct){
				child.giveOriginal(decoded);
			}
		}catch(IllegalArgumentException e){
			correct = false;
		}
	}

	@Override
	public BitSet2 getConverted() {
		return codec.ParityBitsCodec.encode(child.getOriginal());
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
			clone.giveOriginal(child.getOriginal());
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

}
