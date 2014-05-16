package util;

import java.util.Collection;
import java.util.BitSet;

/**
 * Contains utility functions for working with bytes.
 */
public abstract class Bytes {
	/** Formats the given byte as an eight-character string. */
	public static String format(byte b) {
		return String.format("%08d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}
	
	public static String format(char b) {
		return String.format("%16s", Integer.toBinaryString((int)b & 0xFFFF));
	}
	
	/** 
	 * Formats the given byte as an eight-character string. 
	 * Only prints the given amount, starting at LSB;
	 */
	public static String format(byte b,int amount) {
		return String.format("%0"+amount+"d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
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
     * Gets a byte from a bit set.
     * @param data The BitSet object that contains the data.
     * @param start The index of the first bit in the byte.
     * @return The requested byte.
     */
    public static byte fromBitSet(BitSet data, int start) {
        // Assert that the data is long enough
        assert (data.size() > start + 8);

        byte b = 0;

        for (int i = 0; i < 8; i++) {
            b = (byte) (b | (((data.get(start + i)) ? 1:0) << (7-i)));
        }

        return b;
    }

    /**
     * Convert a byte to a bit set with a given size, and puts the byte on a
     * given offset.
     * @param b The byte to convert.
     * @param size The size of the new BitSet object (in bits).
     * @param offset The bit number where to start the byte.
     * @return The new BitSet object.
     */
    public static BitSet toBitSet(byte b, int size, int offset) {
        BitSet data = new BitSet(size);

        for (int i = 0; i < 8; i++) {
            data.set(i + offset, ((b >> (7-i)) & 1) == 1);
        }

        return data;
    }
}
