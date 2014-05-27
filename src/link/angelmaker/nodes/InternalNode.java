package link.angelmaker.nodes;


/**
 * Nodes that implement this Interface recognize the ability to have childNodes.
 * No guarantees given if it actually does have childNodes.
 * @author I3anaan
 *
 */
public interface InternalNode extends Node{
	
	/**
	 * @return the childNodes of this InternalNode.
	 * |result| is not specified, this may be 0.
	 */
	public Node[] getChildNodes();
}
