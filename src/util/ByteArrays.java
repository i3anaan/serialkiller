package util;

import java.util.List;

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
     * Convert a byte array to a BitSet2 object.
     * This method becomes obsolete once Java 7 is available, then
     * BitSet2.valueOf(byte[] bytes) is preferred.
     * @param bytes The byte array to convert.
     * @return The BitSet2 object.
     */
    public static BitSet2 toBitSet(byte[] bytes) {
        return toBitSet(bytes, bytes.length, 0);
    }

        /**
         * Convert a byte array to a BitSet2 object.
         * This method becomes obsolete once Java 7 is available, then
         * BitSet2.valueOf(byte[] bytes) is preferred.
         * @param bytes The byte array to convert.
         * @param size The size of the new BitSet2 object.
         * @param offset The offset.
         * @return The BitSet2 object.
         */
        public static BitSet2 toBitSet(byte[] bytes, int size, int offset) {
        BitSet2 data = new BitSet2(size);

        for (int i = 0; i < bytes.length; i++) {
            for (int j = 0; j < 8; j++) {
                boolean val = ((bytes[i] >> (7-j)) & 1) == 1;
                data.set((i*8)+j+offset, val);
            }
        }

        return data;
    }

    /**
     * Convert a BitSet2 object to a byte array.
     * This method becomes obsolete once Java 7 is available, then
     * BitSet2.toByteArray() is preferred.
     * @param data The BitSet2 object to convert.
     * @return The byte array.
     */
    public static byte[] fromBitSet(BitSet2 data) {
        return fromBitSet(data, (int) Math.ceil((double) data.length() / 8));
    }

    /**
     * Convert a BitSet2 object to a byte array.
     * This method becomes obsolete once Java 7 is available, then
     * BitSet2.toByteArray() is preferred.
     * @param data The BitSet2 object to convert.
     * @param len The length of the resulting array.
     * @return The byte array.
     */
    public static byte[] fromBitSet(BitSet2 data, int len) {
        int rlen = (int) Math.ceil((double) data.length() / 8);
        assert (len >= rlen);
        byte[] bytes = new byte[len];

        for (int i = 0; i < len; i++) {
            for (int j = 0; j < 8; j++) {
                byte bit = (data.get((i*8)+j)) ? (byte) 1 : (byte) 0;
                bytes[i] = (byte) (bytes[i] | (bit << (7-j)));
            }
        }

        return bytes;
    }

	/**
	 * Converts a list of Byte (not the primitive) to a byte[]
	 * Will return null if the given list is null, or it contains a null.
	 * @param arr
	 * @return
	 */
	public static byte[] fromList(List<Byte> arr) {
		if (arr == null) {
			return null;
		}
		final int n = arr.size();
		byte ret[] = new byte[n];
		for (int i = 0; i < n; i++) {
			if (arr.get(i) == null) {
				return null;
			}
			ret[i] = arr.get(i);
		}
		return ret;
	}
}
