package link.angelmaker.nodes;

import link.angelmaker.bitexchanger.BitExchanger;

/**
 * An interface to indicate that this Node can build itself if given a BitExchanger.
 * @author I3anaan
 *
 */
public interface SelfBuildingNode extends Node {

	public interface Duplex extends SelfBuildingNode{
		/**
		 * Hands the BitExchanger instance to the Node, to build itself.
		 * This way the Node has more control on how to build itself.
		 * (With aspect to blocking, reacting to received bits, etc)
		 * 
		 * The node that this method is called on will change it self, to match the received node.
		 * It needs a Node to send to the other side.
		 * 
		 * @param exchanger	The BitExchanger to communicate on.
		 * @param nodeToSend	The Node containing data to be send.
		 */
		public void buildSelf(BitExchanger exchanger,Node nodeToSend);
	}
}
