package link.angelmaker.bitexchanger;

import link.angelmaker.nodes.Node;
import link.angelmaker.nodes.NotSupportedNodeException;

/**
 * Interface for a BitExchangerManager.
 * Takes a BitExchanger and Node (For example in the constructor)
 * and connects these.
 * Connecting means that the Converted data from the Node will be sent over the BitExchanger.
 * 
 * The implementation decides whether it is blocking or not.
 * 
 * @author I3anaan
 *
 */
public interface BitExchangerManager {
	
	/**
	 * Sends the Converted data from a node.
	 * @param node	Node to send.
	 */
	public void sendNode(Node node)  throws NotSupportedNodeException;
	
	/**
	 * @return A completed Node.
	 * Can be null, usually depends on whether its blocking or not
	 */
	public Node readNode();
}
