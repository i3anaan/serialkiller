package link;

import common.Layer;

/**
 * Basic interface for implementations of link layers. This interface specifies
 * the basic API to ensure compatibility independent of the type of link layer
 * that is used.
 */
public abstract class LinkLayer extends Layer {

	/**
	 * Sends the given byte over the link.
	 * 
	 * @param data
	 *            The data to send.
	 */
	public abstract void sendByte(byte data);

	/**
	 * Reads a byte from the link.
	 * 
	 * @return The received byte
	 */
	public abstract byte readByte();

    public abstract void sendFrame(byte[] data);
    public abstract byte[] readFrame();

}
