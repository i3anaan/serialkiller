package util;

import util.BitSet2;
import java.util.Collection;

/**
 * Contains utility functions for working with bytes.
 */
public abstract class Bytes {
	/** Formats the given byte as an eight-character string. */
	public static String format(byte b) {
		return String.format("%08d",
				Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}

	/**
	 * Formats the given byte as an eight-character string. Only prints the
	 * given amount, starting at LSB;
	 * */
	public static String format(byte b, int amount) {
		return String.format("%0" + amount + "d",
				Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}

	/**
	 * Gets a byte from a bit set.
	 * 
	 * @param data
	 *            The BitSet2 object that contains the data.
	 * @param start
	 *            The index of the first bit in the byte.
	 * @return The requested byte.
	 */
	public static byte fromBitSet(BitSet2 data, int start) {
		// Assert that the data is long enough
		assert (data.size() > start + 8);

		byte b = 0;

		for (int i = 0; i < 8; i++) {
			b = (byte) (b | (((data.get(start + i)) ? 1 : 0) << (7 - i)));
		}

		return b;
	}

	/**
	 * Convert a byte to a BitSet2 object. This method becomes obsolete once
	 * Java 7 is available, then BitSet2.valueOf(byte b) is preferred.
	 * 
	 * @param bytes
	 *            The byte to convert.
	 * @return The BitSet2 object.
	 */
	public static BitSet2 toBitSet(byte b) {
		BitSet2 data = new BitSet2(8);

		for (int j = 0; j < 8; j++) {
			boolean val = ((b >> (7 - j)) & 1) == 1;
			data.set(j, val);
		}

		return data;
	}

	/** Turns a collection of numbers into an array of bytes. Based on Guava's Bytes.toArray. */
	public static byte[] toArray(Collection<? extends Number> collection) {
      Object[] boxedArray = collection.toArray();
      int len = boxedArray.length;
      
      byte[] array = new byte[len];
      for (int i = 0; i < len; i++) {
        array[i] = ((Number) boxedArray[i]).byteValue();
      }
      return array;
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
     * Convert a byte to a bit set with a given size, and puts the byte on a
     * given offset.
     * @param b The byte to convert.
     * @param size The size of the new BitSet object (in bits).
     * @param offset The bit number where to start the byte.
     * @return The new BitSet object.
     */
    public static BitSet2 toBitSet2(byte b, int size, int offset) {
        BitSet2 data = new BitSet2(size);

        for (int i = 0; i < 8; i++) {
            data.set(i + offset, ((b >> (7-i)) & 1) == 1);
        }

        return data;
    }
}
