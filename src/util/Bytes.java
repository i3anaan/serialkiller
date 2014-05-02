package util;

/**
 * Contains utility functions for working with bytes.
 */
public abstract class Bytes {
	/** Formats the given byte as an eight-character string. */
	public static String format(byte b) {
		return String.format("%08d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
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
}
