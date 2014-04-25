package util;

/**
 * Contains utility functions for working with bytes.
 */
public abstract class Bytes {
	/** Formats the given byte as an eight-character string. */
	public static String format(byte b) {
		return String.format("%08d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}
	/** Formats the given byte as an eight-character string. 
	 * Only prints the given amount, starting at LSB;
	 * */
	public static String format(byte b,int amount) {
		return String.format("%0"+amount+"d", Integer.parseInt(Integer.toBinaryString(b & 0xFF)));
	}
}
