package util;

import util.BitSet2;

/**
 * Contains utilities for working with byte arrays.
 */
public class ByteArrays {
    /**
     * Parses a byte array to a long.
     * @param bytes The byte array.
     * @return The numeric (long) value.
     */
    public static long parseLong(byte[] bytes) {
        long val = 0;

        for (int i = 0; i < bytes.length; i++) {
            val = (val << 8) + (bytes[i] & 0xff);
        }

        return val;
    }

    /**
     * Shifts bits across bytes. The resulting byte array will have one more
     * element than the input, unless the amount of places to shift is zero.
     * @param bytes The bytes to shift on.
     * @param shift The amount of places to shift to the left. Shifts to the
     *              right if negative.
     * @return The byte array containing the shifted data, padded with 0s.
     */
    public static byte[] bitshift(byte[] bytes, int shift) {
        byte[] result;

        if (shift != 0) {
            result = new byte[bytes.length + 1];

            for (int i = 0; i < bytes.length; i++) {
                byte b = bytes[i];
                byte target0, target1;

                if (shift < 0) {
                    // Shift to the right
                    target0 = (byte) (b >> (7 + shift)); // MSB part
                    target1 = (byte) (b << -shift); // LSB part
                } else {
                    // Shift to the left
                    target0 = (byte) (b >> shift); // MSB part
                    target1 = (byte) (b << (7 - shift)); // LSB part
                }

                result[i] = (byte) (result[i] | target0);
                result[i + 1] = target1;
            }
        } else {
            result = bytes;
        }

        return result;
    }

    

    
}
