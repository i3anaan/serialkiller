package link.angelmaker.nodes;
import util.BitSet2;

/**
 * The Node interface. This class acts as a conversion function.
 * Instead of the normal layer like structure, nodes support a tree structure.
 * This creates higher potential functionality, however at the cost of computing time, memory and clearness.
 * As a tree is able to split the data, it needs to also be able to recombine it.
 * To be able to do this, the Node needs to be able to store data internally.
 * Also this means that the Node cannot simply be used as a single conversion Function, it needs a state.
 * 
 * At any point in time the state of the Node has 2 representations: Original and Converted.
 * Original is just the original data that was put in, this usually is stored in the lowest Node.
 * Converted is a serialized version of the Node, usually more resilient to errors.
 * 
 * The original should always be the same as the original given.
 * When full, getting the converted and giving it to an empty node should create the exact same state.
 * @author I3anaan
 */
public interface Node {

	/**
	 * Give the Node original bits. Generally the Node will >NOT< try to correct
	 * errors in this input. Similar to giveConverted(); Will return the unused
	 * bits in a >NEW< bitset2. (so if you give it more than it needs,it returns
	 * the unused rest which can then for example be given to the next Node).
	 * This method should not consume any bits when isFull() holds.
	 * 
	 * @require bits!=null;
	 * @ensure result!=null;
	 * @ensure Let bits consist of xy (with both being a BitSequence) x is the
	 *         part internally consumed, y is left over, unused. |x| +|y| =
	 *         |bits| will return y, 0<=|y|<=|bits| 0<=|x|<=|bits|
	 */
	public BitSet2 giveOriginal(BitSet2 bits);

	/**
	 * Get the original, not encoded, data.
	 * 
	 * @return The bits stored in this Node.
	 * @ensure result!=null No guarantees are given about this result when
	 *         !isComplete().
	 */
	public BitSet2 getOriginal();

	/**
	 * Give the Node bits that were previously converted, or received converted.
	 * Generally the Node will try to correct errors in this input. These converted strings can also hold extra state information Similar to
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
	 * resilient or include extra state information. (Usually because these bits contain redundancy).
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
	 * Not all nodes will always need to be completely filled before being ready to send, isReady determines whether or not the Node considers itself ready to send.
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
	 * @return array of the children, can be or contain null.
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
	
	/**
	 * Interface to indicate the Node only accepts a one time injection of data, then considers itself full.
	 * This interface has no method, but further specifies the contract of the give/get data methods.
	 * @author I3anaan
	 *
	 */
	public interface OneTimeInjection extends Node{
		
	}
	
	public interface Resetable extends Node {
		/**
		 * Reset the Node, same as building a new node (except more memory efficient).
		 */
		public void reset();
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
}
