package link.angelmaker.nodes;

import util.BitSet2;

/**
 * A simple Node class.
 * It supports a variable amount of bits.
 * Is able to receive the bits in parts.
 * Does not do any encoding.
 * @author I3anaan
 */
public class PureNode implements Node.Leaf {

	//First 8 indices are data bits, index 8 contains special flag.
	private BitSet2 stored;
	private int dataBitCount;
	private Node parent;
	
	public PureNode(Node parent, int dataBitCount){
		stored = new BitSet2();
		this.dataBitCount = dataBitCount;
		this.parent = parent;
	}
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		int i;
		for(i=0;i<bits.length() && stored.length()<dataBitCount;i++){
			stored.addAtEnd(bits.get(i));
		}
		return bits.get(i,bits.length());
	}

	@Override
	public BitSet2 getOriginal() {
		return stored;
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		return giveOriginal(bits);
	}

	@Override
	public BitSet2 getConverted() {
		return getOriginal();
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public boolean isComplete() {
		return stored.length()==dataBitCount;
	}

	@Override
	public boolean isCorrect() {
		return true;
	}

	@Override
	public Node getClone() {
		Node clone = new PureNode(parent,dataBitCount);
		clone.giveConverted(stored);
		return clone;
	}
	@Override
	public String toString(){
		return "PureNode["+stored+"]";
	}
}
