package link.angelmaker.nodes;

import link.angelmaker.bitexchanger.BitExchanger;
import util.BitSet2;

/**
 * The basic Node class. This class accepts a BitSet2, stores a BitSet2
 * internally, and can then give a BitSet2 back. These 3 BitSet2 can be
 * different. The implementation can fully decide what to do with the bits,
 * including the storage method. The implementation can either be a leaf or have
 * child nodes itself. The implementation may or may not have error detection or
 * correction.
 * 
 * A Node that isReady() but not isFull() should be a flag, the other way around
 * is not set in stone.
 * 
 * The following pieces of pseudo code should hold (with no errors occurring): n
 * = giveConverted(n.getConverted());
 * 
 * n = giveOriginal(n.getOriginal());
 * 
 * sent.giveOriginal(data) received.giveConverted(sent.getConverted()); data =
 * received.getOriginal();
 * 
 * sent.giveConverted(data) received.giveOriginal(sent.getOriginal()); data =
 * received.getConverted();
 * 
 * In diagram form:
 * 
 * [ 1 0 1 ] giveOriginal() [111 000 111] getConverted()
 * 
 * V
 * 
 * [111 000 111] giveConverted() [ 1 0 1 ] getOriginal()
 * 
 * V
 * 
 * [ 1 0 1 ] giveOriginal() [111 000 111] getConverted() etc...
 * 
 * 
 * This should NOT be implemented directly. Implement indirectly through either
 * LeafNode or InternalNode, or both.
 * 
 * @author I3anaan
 */
public interface Node {

	/**
	 * Give the Node original bits. Generally the Node will >NOT< try to correct
	 * errors in this input. Similar to giveConverted(); Will return the unused
	 * bits in a >NEW< bitset2. (so if you give it more than it needs,it returns
	 * the unused rest which can then for example be given to the next Node).
	 * This method should not consume any bits when isComplete() holds.
	 * 
	 * @require bits!=null;
	 * @ensure result!=null;
	 * @ensure Let bits consist of xy (with both being a BitSequence) x is the
	 *         part internally consumed, y is left over, unused. |x| +|y| =
	 *         |bits| will return y, 0<=|y|<=|bits| 0<=|x|<=|bits|
	 */
	public BitSet2 giveOriginal(BitSet2 bits);

	/**
	 * Get the original bits from the Node Does it best to correct the bits
	 * before returning.
	 * 
	 * @return The bits stored in this Node.
	 * @ensure result!=null No guarantees are given about this result when
	 *         !isComplete().
	 */
	public BitSet2 getOriginal();

	/**
	 * Give the Node bits that were previously converted, or received converted.
	 * Generally the Node will try to correct errors in this input. Similar to
	 * giveOriginal(); Will return the unused bits. (so if you give it more than
	 * it needs,it returns the unused rest which can then for example be given
	 * to the next Node). This method should not consume any bits when
	 * isComplete() holds.
	 * 
	 * @require bits!=null;
	 * @ensure result!=null;
	 * @ensure Let bits consist of xy (with both being a BitSequence) x is the
	 *         part internally consumed, y is left over, unused. |x| +|y| =
	 *         |bits| will return y, 0<=|y|<=|bits| 0<=|x|<=|bits|
	 */
	public BitSet2 giveConverted(BitSet2 bits);

	/**
	 * Get the converted bits from the Node. Generally these bits are more error
	 * resilient. (Usually because these bits contain redundancy).
	 * 
	 * @return The bits stored in this Node.
	 * @ensure result!=null No guarantees are given about this result when
	 *         !isComplete().
	 */
	public BitSet2 getConverted();

	/**
	 * @return The parent of this Node, or null if it does not have a parent.
	 */
	public Node getParent();

	/**
	 * @return Whether or not this Node has its dataStorage filled, meaning it
	 *         no longer accepts new data. (it has filled its dataStorage). Does
	 *         not in any way has something to do with isCorrect().
	 */
	public boolean isFull();

	/**
	 * While this usually will be the same as isFull(), in some cases a Node
	 * might be a flag. A flag Node will not always consider itself to be full,
	 * but it is ready to send.
	 * 
	 * @return Whether or not this Node is ready to be either send or read out.
	 */
	public boolean isReady();

	/**
	 * @return Whether or not this Node considers itself correct. This is based
	 *         on the current state, the currently stored data. The result of
	 *         this is obviously limited by the errorDetection used internally.
	 *         If the Node implementation does not have a form of
	 *         errorDetection, this should return true.
	 */
	public boolean isCorrect();

	/**
	 * @return An identical (but separate) clone of this instance. Be very wary
	 *         when using this, the clone will have the same parent as the Node
	 *         that was cloned. However, the parent only considers the non-clone
	 *         to be its child. Realistically should only be used on top level
	 *         Nodes, ie where parent==null.
	 */
	public Node getClone();
	
	/**
	 * @return array of the childs, can be or contain null.
	 */
	public Node[] getChildNodes();

	/**
	 * This method is somewhat optional, AngelMaker should not depend on this
	 * method returning something specific. It should be purely bonus
	 * information, to help oversee things while debugging.
	 * 
	 * @return A short message of the state of the Node. This is used for
	 *         visualization and can contain things like: full, flag, incorrect,
	 *         etc...
	 */
	public String getStateString();

	/*
	 * Following are some extensions to the interface. Node implementations
	 * should ALWAYS implement at least 1 of these. These however are not
	 * mutually exclusive, and a single class may implement all.
	 */

	
	//TODO leaf/internal interfaces do nothing.
	/**
	 * A Node that does NOT have child nodes itself, it is a leaf.
	 * 
	 * @author I3anaan
	 * 
	 */
	public interface Leaf extends Node {

	}

	/**
	 * Nodes that implement this Interface recognize the ability to have
	 * childNodes. No guarantees given if it actually does have childNodes.
	 * 
	 * @author I3anaan
	 * 
	 */
	public interface Internal extends Node {

		/**
		 * @return the childNodes of this InternalNode. |result| is not
		 *         specified, this may be 0.
		 */
		//public Node[] getChildNodes();
	}

	/**
	 * An Interface to indicate whether or not this Node has the ability to send
	 * filler data. This ability means calling getConverted() on an empty node
	 * returns a non empty BitSet2. And when inputting received FillerData in
	 * getOriginal(), it wont return anything.
	 * 
	 * @author I3anaan
	 * 
	 */
	public interface Fillable extends Node {
		public boolean isFiller();
		public Node getFiller();
	}

	/**
	 * An interface to indicate that this Node can build itself if given a
	 * BitExchanger.
	 * 
	 * @author I3anaan
	 * 
	 */
	public interface SelfBuilding extends Node {

		/**
		 * Hands the BitExchanger instance to the Node, to build itself. This
		 * way the Node has more control on how to build itself. (With aspect to
		 * blocking, reacting to received bits, etc)
		 * 
		 * The node that this method is called on will change it self, to match
		 * the received node. It needs a Node to send to the other side.
		 * 
		 * @param exchanger
		 *            The BitExchanger to communicate on.
		 * @param nodeToSend
		 *            The Node containing data to be send.
		 */
		public void buildSelf(BitExchanger exchanger, Node nodeToSend);

		/**
		 * @author I3anaan
		 * @Requires BitExchanger.MasterSlave
		 */
		public interface TurnedBased extends SelfBuilding {

		}
	}

}
