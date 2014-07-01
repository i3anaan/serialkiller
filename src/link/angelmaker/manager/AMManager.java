package link.angelmaker.manager;

import util.BitSet2;
import link.angelmaker.bitexchanger.BitExchanger;
import link.angelmaker.nodes.Node;

/**
 * Interface for an AngelMakerManager (AMManager). Takes a BitExchanger and Node
 * (For example in the constructor) and connects these. Connecting means that
 * the Converted data from the Node will be sent over the BitExchanger.
 * 
 * The implementation decides whether it is blocking or not.
 * 
 * For correct usage, construct first, using the empty constructor. Then set a
 * non-null exchanger using setExchanger() Then call enable();
 * 
 * @author I3anaan
 * 
 */
public interface AMManager {

	/**
	 * Sets the BitExchanger to use. This has to be done this way, as first the
	 * AMManager will be build, then the BitExchanger (which might need the
	 * manager). Then finally that BitExchanger is given to the AMManager.
	 * 
	 * @param exchanger
	 */
	public void setExchanger(BitExchanger exchanger);

	/**
	 * Enables the AMManager, letting it start sending data. Before enabling,
	 * calling the different methods have absolutely no guarantees.
	 */
	public void enable();

	/**
	 * Give the AMManager bytes (data) to be send. Its up to the AMManager to
	 * decide how and when to send this. Usually however this gets put into a
	 * Node, and then the converted data will be gotten from that node and this
	 * converted data will be send over the BitExchanger.
	 * 
	 * @param bytes
	 *            bytes[] to send.
	 */
	public void sendBytes(byte[] bytes);

	/**
	 * Only returns every byte only once (acts as sort of a queue).
	 * @return new unread bytes received by the Manager, or empty array when nothing new.
	 */
	public byte[] readBytes();

	/**
	 * @return The Node (most likely) currently being Send. Can return null if
	 *         no Node send yet or unknown.
	 */
	public Node getCurrentSendingNode();

	/**
	 * @return The Node (most likely) currently being Received. Can return null
	 *         if no Node received yet or unknown.
	 */
	public Node getCurrentReceivingNode();

	// Extension interfaces

	/**
	 * This AMManager works a bit different. Instead of just pushing the Nodes
	 * to the BitExchanger, this manager waits and also gives a Node when asked
	 * for one. This is useful (needed) for BitExchanger.AlwaysSending, as these
	 * require filler bits if there is no data to send.
	 * 
	 * @author I3anaan
	 */
	public interface Server extends AMManager {
		public BitSet2 getNextBits();
	}
}
