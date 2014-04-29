package util;

/**
 * Contains utility functions for working with bytes.
 */
public abstract class Bytes {
	/** Formats the given byte as an eight-character string. */
	public static String format(byte b) {
		return String.format("%08d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}
}
