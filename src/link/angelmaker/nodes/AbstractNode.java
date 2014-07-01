package link.angelmaker.nodes;

import util.BitSet2;

/**
 * Basic abstract implementation of the Node class.
 * Typically alot of these methods will have to be overwritten.
 * @author I3anaan
 *
 */
public abstract class AbstractNode implements Node {

	protected Node[] children = null;
	protected Node parent;
	
	@Override
	public abstract BitSet2 giveOriginal(BitSet2 bits);

	@Override
	public BitSet2 getOriginal() {
		return children[0].getOriginal();
	}

	@Override
	public abstract BitSet2 giveConverted(BitSet2 bits);

	@Override
	public abstract BitSet2 getConverted();

	@Override
	public Node getParent() {
		return parent;
	}

	@Override
	public abstract boolean isFull();

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
		//TODO correct this way?
		Node clone = null;
		try {
			clone = this.getClass().newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		clone.giveConverted(this.getConverted());
		return clone;
	}

	@Override
	public Node[] getChildNodes() {
		return children;
	}

	@Override
	public String getStateString() {
		return "Not specified";
	}

}
