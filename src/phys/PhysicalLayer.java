package phys;

/**
 * Basic interface for implementations of physical layers. SerialKiller is only
 * intended to run on Telematica's special kind of serial connection, which
 * means that a subclass of HardwareLayer is most probably the layer you'll be
 * using. However, this package contains some alternative and additional layer
 * implementations that can be useful for testing and troubleshooting.
 * 
 * Note that the physical layer is not responsible for any kind of delimiting or
 * error correction whatsoever. For example, readByte can block, or it can
 * return the same result twice, or it can return something entirely wrong. This
 * is to be expected - and corrected in higher layers.
 * 
 */
public interface PhysicalLayer {
	/**
	 * Sends the two lowest-order bits in the given byte to whatever is backing
	 * this layer.
	 */
	public void sendByte(byte data);

	/**
	 * Reads a pair of bits from the link and return them as the two
	 * lowest-order bits in the returned byte.
	 * 
	 * @return The received byte
	 */
	public byte readByte();
}
