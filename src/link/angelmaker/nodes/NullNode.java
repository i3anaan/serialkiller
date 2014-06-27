package link.angelmaker.nodes;

import util.BitSet2;

public class NullNode implements Node{

	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		return new BitSet2();
	}

	@Override
	public BitSet2 getOriginal() {
		// TODO Auto-generated method stub
		return new BitSet2();
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		return new BitSet2();
	}

	@Override
	public BitSet2 getConverted() {
		return new BitSet2();
	}

	@Override
	public Node getParent() {
		return null;
	}

	@Override
	public boolean isFull() {
		return false;
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public boolean isCorrect() {
		return false;
	}

	@Override
	public Node getClone() {
		return new NullNode();
	}

	@Override
	public Node[] getChildNodes() {
		return null;
	}

	@Override
	public String getStateString() {
		return "NULL NODE";
	}

	@Override
	public void reset() {
	}

}
