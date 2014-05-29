package link.angelmaker.manager;

import link.angelmaker.bitexchanger.BitExchanger;
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
 * For correct usage, construct first, using the empty constructor.
 * Then set a non-null exchanger using setExchanger()
 * Then call enable();
 * The AMManger is not ready for usage.
 * 
 * @author I3anaan
 *
 */
public interface AMManager {
	
	/**
	 * Sets the BitExchanger to use.
	 * This has to be done this way, as first the AMManager will be build,
	 * then the BitExchanger (which might need the manager).
	 * Then finally that BitExchanger is given to the AMManager.
	 * @param exchanger
	 */
	public void setExchanger(BitExchanger exchanger);
	/**
	 * Enables the AMManager.
	 * Before enabling, calling the different methods have absolutely no guarantees.
	 * They might crash, they might work, they might do nothing.
	 */
	public void enable();
	
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
	
	
	/**
	 * This AMManager works a bit different.
	 * Instead of just pushing the Nodes to the BitExchanger, this manager waits and only gives a Node when asked for one.
	 * This is useful (needed) for BitExchanger.AlwaysSending, as these require FillerData if there is no data to send.
	 * @author I3anaan
	 */
	public interface Server extends AMManager{
		public Node getNextNode();
	}
}