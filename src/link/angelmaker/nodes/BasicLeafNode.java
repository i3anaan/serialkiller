package link.angelmaker.nodes;

import util.BitSet2;

/***
 *    ██████╗ ███████╗██████╗ ██████╗ ███████╗ ██████╗ █████╗ ████████╗███████╗██████╗ 
 *    ██╔══██╗██╔════╝██╔══██╗██╔══██╗██╔════╝██╔════╝██╔══██╗╚══██╔══╝██╔════╝██╔══██╗
 *    ██║  ██║█████╗  ██████╔╝██████╔╝█████╗  ██║     ███████║   ██║   █████╗  ██║  ██║
 *    ██║  ██║██╔══╝  ██╔═══╝ ██╔══██╗██╔══╝  ██║     ██╔══██║   ██║   ██╔══╝  ██║  ██║
 *    ██████╔╝███████╗██║     ██║  ██║███████╗╚██████╗██║  ██║   ██║   ███████╗██████╔╝
 *    ╚═════╝ ╚══════╝╚═╝     ╚═╝  ╚═╝╚══════╝ ╚═════╝╚═╝  ╚═╝   ╚═╝   ╚══════╝╚═════╝ 
 *                                                                                     
 */

/**
 * The most basic leaf node possible.
 * Does not error correction, thus converted and original are equal.
 * Accepts a 1 time insertion, then considers itself full.
 * @author I3anaan
 */
public class BasicLeafNode implements Node.Leaf {

	private BitSet2 stored;
	private Node parent;
	
	public BasicLeafNode(Node parent){
		this.parent = parent;
		stored = new BitSet2();
	}
	
	@Override
	public BitSet2 giveOriginal(BitSet2 bits) {
		if(this.stored.length()==0){
			stored = bits;
			return new BitSet2();
		}else{
			return new BitSet2(bits);
		}
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
	public boolean isFull() {
		return stored.length()!=0;
	}

	@Override
	public boolean isCorrect() {
		return true;
	}

	@Override
	public Node getClone() {
		BasicLeafNode node = new BasicLeafNode(parent);
		
		return node;
	}
	

	@Override
	public String toString(){
		return "BasicLeafNode["+stored+"]";
	}

	@Override
	public boolean isReady() {
		return isFull();
	}
	
	@Override
	public String getStateString() {
		String s;
		if (this.isFull()) {
			s = "Full+Ready";
		} else {
			s = "unkown";
		}
		return s;
	}

}
