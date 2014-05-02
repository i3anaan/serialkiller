package util;

import java.util.BitSet;

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

    /**
     * Convert a byte array to a BitSet object.
     * This method becomes obsolete once Java 7 is available, then
     * BitSet.valueOf(byte[] bytes) is preferred.
     * @param bytes The byte array to convert.
     * @return The BitSet object.
     */
    public static BitSet toBitSet(byte[] bytes) {
        return toBitSet(bytes, bytes.length * 8, 0);
    }

    /**
     * Convert a byte array to a BitSet object.
     * @param bytes The byte array to convert.
     * @param size The size of the BitSet object.
     * @param offset The offset where to place the byte array.
     * @return The BitSet object.
     */
    public static BitSet toBitSet(byte[] bytes, int size, int offset) {
        BitSet data = new BitSet(size);

        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                boolean val = ((bytes[i] >> (7-j)) & 1) == 1;
                data.set((i*8)+j+offset, val);
            }
        }

        return data;
    }

    /**
     * Convert a BitSet object to a byte array.
     * This method becomes obsolete once Java 7 is available, then
     * BitSet.toByteArray() is preferred.
     * @param data The BitSet object to convert.
     * @return The byte array.
     */
    public static byte[] fromBitSet(BitSet data) {
        int len = (int) Math.ceil((double) data.length() / 8);
        byte[] bytes = new byte[len];

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < 8; j++) {
                byte bit = (data.get((i*8)+j)) ? (byte) 1 : (byte) 0;
                bytes[i] = (byte) (bytes[i] | (bit << (7-j)));
            }
        }

        return bytes;
    }
}
