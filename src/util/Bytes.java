package util;

import java.util.Collection;

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
}
