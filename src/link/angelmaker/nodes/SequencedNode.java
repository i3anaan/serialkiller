package link.angelmaker.nodes;

import util.BitSet2;

public class SequencedNode implements Node, Node.Internal {

	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BitSet2 getOriginal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BitSet2 giveConverted(BitSet2 bits) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BitSet2 getConverted() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node getParent() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isFull() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isReady() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCorrect() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Node getClone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Node[] getChildNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStateString() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public int getSeq(){
		return 0; //TODO
	}
	
	public int getMessage(){
		return 0; //TODO
	}

}
