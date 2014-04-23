package phys;

/**
 * A hardware layer is physical layer backed by actual serial port hardware.
 * 
 * This class contains utilities for doing some useful bit-twiddling that would
 * be common to all hardware layer implementations. In particular, it provides
 * functions to shuftle bytes.
 * 
 * Shuftling (from 'shuffle' and 'shift') is an operation that the Telematica
 * serial driver seems to do, mapping bit positions on the input to bit
 * positions on the output. The class contains utility functions both to
 * simulate and to reverse this process. The latter is used by the
 * implementations of readByte.
 */
public abstract class HardwareLayer extends PhysicalLayer {

	/**
	 * Shufts the given byte to the right, reversing the operation done by the
	 * Telematica driver.
	 */
	public static byte shuftRight(byte in) {
		byte byte0 = (byte) ((in >> 5) & 1);
		byte byte1 = (byte) ((in >> 3) & 2);
		return (byte) (0 | byte0 | byte1);
	}

	/**
	 * Shufts the given byte to the left, simulating the operation done by the
	 * Telematica driver.
	 */
	public static byte shuftLeft(byte in) {
		byte byte0 = (byte) ((in & 1) << 5);
		byte byte1 = (byte) ((in & 2) << 3);
		return (byte) (0 | byte0 | byte1);
	}
}
