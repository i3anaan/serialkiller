package network;

import common.Layer;



	public abstract class NetworkLayer extends Layer {

		/**
		 * Sends the given packet over the network.
		 * 
		 * @param data
		 *            The data to send.
		 */
		public abstract void sendPacket(Packet data);

		/**
		 * Reads a packet from the network.
		 * 
		 * @return The received packet
		 */
		public abstract byte readPacket();

	}

