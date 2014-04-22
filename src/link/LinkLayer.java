package link;

/**
 * Basic interface for implementations of link layers. This interface specifies the basic API to ensure compatibility
 * independent of the type of link layer that is used.
 */
public interface LinkLayer {

    /**
     * Sends the given byte over the link.
     * @param data The data to send.
     */
    public void sendByte(byte data);

    /**
     * Reads a byte from the link.
     * @return The received byte
     */
    public byte readByte();

}
