package util;

import util.BitSet2;

/**
 * Contains utility functions for working with bytes.
 */
public abstract class Bytes {
	/** Formats the given byte as an eight-character string. */
	public static String format(byte b) {
		return String.format("%08d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}
    
	/** 
     * Formats the given byte as an eight-character string. 
	 * Only prints the given amount, starting at LSB;
	 */
	public static String format(byte b,int amount) {
		return String.format("%0"+amount+"d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}

    /**
     * Parses a bit in a byte into a boolean.
     * @param b The byte.
     * @param n The bit number (0-7).
     * @return The boolean represented by the bit in the byte.
     */
    public static boolean parseBoolean(byte b, int n) {
        assert (n >= 0 && n <= 7);

        byte bit = (byte) ((b << n) >> 7);

        return ((bit & 1) == 1);
    }

    /**
     * Gets a byte from a bit set.
     * @param data The BitSet2 object that contains the data.
     * @param start The index of the first bit in the byte.
     * @return The requested byte.
     */
    public static byte fromBitSet(BitSet2 data, int start) {
        // Assert that the data is long enough
        assert (data.size() > start + 8);

        byte b = 0;

        for (int i = 0; i < 8; i++) {
            b = (byte) (b | (((data.get(start + i)) ? 1:0) << (7-i)));
        }

        return b;
    }
}
