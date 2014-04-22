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
public abstract class HardwareLayer implements PhysicalLayer {

	/**
	 * Shufts the given byte to the right, reversing the operation done by the
	 * Telematica driver.
	 */
	protected byte shuftRight(byte in) {
		return (byte) 0;
	}

	/**
	 * Shufts the given byte to the left, simulating the operation done by the
	 * Telematica driver.
	 */
	protected byte shuftLeft(byte in) {
		return (byte) 0;
	}
}
