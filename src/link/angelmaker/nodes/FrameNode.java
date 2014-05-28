package link.angelmaker.nodes;

import util.BitSet2;

/**
 * Basic FrameNode class. Contains an array of Nodes, the Node type is specified
 * through generics. The amount of Nodes held is specified in the constructor.
 * 
 * It does not use an error code on its own level. After constructing this Node,
 * but before giving it bits this Node is considered an Filler. It does this by
 * appending a single bit to the bits it gets, and placing that in the
 * underlying Nodes. Meaning that the array of underlying nodes stores 1 more
 * bit (flag bit) than this Node.getOriginal() returns.
 * 
 * If the first bit in the first childNode is 1, it is considerd to be a filler
 * flag. This Node will then return an empty BitSet2
 * 
 * @author I3anaan
 * 
 * @param <N>
 *            The type of child to hold, needs to at least be of type Node.
 */
public class FrameNode<N extends Node> implements Node.Fillable, Node.Internal {

	private Node[] nodes;
	private Node parent;
	private boolean unchanged;
	private static final BitSet2 FLAG_FILLER = new BitSet2(new boolean[] {
			true, true, true, true, true, true });

	public FrameNode(Node parent, int childNodeCount) {
		this.parent = parent;
		nodes = new Node[childNodeCount];
		for(int i=0;i<nodes.length;i++){
			nodes[i] = new PureNode(this,8);
			//TODO build childNode from type N > ask Wander.
		}
		unchanged = true;
	}

	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		if (unchanged && bits.length() > 0) {
			nodes[0].giveOriginal(new BitSet2(new boolean[] { false }));
			unchanged = false;
		}

		int taken = 0;
		BitSet2 bitsLeft = (BitSet2) bits.clone();
		for (int i = 0; i < nodes.length && taken < bits.length(); i++) {
			if (!nodes[i].isComplete()) {
				bitsLeft = nodes[i].giveOriginal(bitsLeft);
			}
		}
		return bitsLeft;
	}

	@Override
	public BitSet2 getOriginal() {
		if (unchanged) {
			return new BitSet2();
		} else {
			BitSet2 result = new BitSet2();
			for (int i = 0; i < nodes.length; i++) {
				result = BitSet2.concatenate(result, nodes[i].getOriginal());
			}
			return result.get(1, result.length());
		}
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		if (unchanged && bits.length() > 0) {
			if (bits.get(0)) {
				// Flag > filler > ignore;
			} else {
				unchanged = false;
			}
		}

		int taken = 0;
		BitSet2 bitsLeft = (BitSet2) bits.clone();
		if (!unchanged) {
			for (int i = 0; i < nodes.length && taken < bits.length(); i++) {
				if (!nodes[i].isComplete()) {
					bitsLeft = nodes[i].giveConverted(bitsLeft);
				}
			}
		}
		return bitsLeft;
	}

	@Override
	public BitSet2 getConverted() {
		if (unchanged) {
			return new BitSet2(FLAG_FILLER);
		} else {
			BitSet2 result = new BitSet2();
			for (int i = 0; i < nodes.length; i++) {
				result = BitSet2.concatenate(result, nodes[i].getConverted());
			}
			return result;
		}
	}

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public boolean isComplete() {
		boolean complete = true;
		for(Node n: nodes){
			if(!n.isComplete()){
				complete = false;
			}
		}
		return complete;
	}

	@Override
	public boolean isCorrect() {
		boolean correct = true;
		for(Node n: nodes){
			if(!n.isCorrect()){
				correct = false;
			}
		}
		return correct;
	}

	@Override
	public Node getClone() {
		FrameNode<N> clone = new FrameNode<N>(parent,nodes.length);
		for(int i=0;i<nodes.length;i++){
			clone.nodes[i] = nodes[i].getClone();
		}
		
		return clone;
	}

	@Override
	public Node[] getChildNodes() {
		return nodes;
	}

	@Override
	public boolean isFiller() {
		return unchanged;
	}

}
