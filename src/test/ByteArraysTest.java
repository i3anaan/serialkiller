package test;

import static org.junit.Assert.*;

import org.junit.Test;

import util.ByteArrays;

import java.nio.ByteBuffer;
import java.util.Arrays;
import util.BitSet2;

/** Unit tests for the ByteArrays class. */
public class ByteArraysTest {

    @Test
    public void testLong() {
        long[] testValues = {1L, 10000L, -10L};

        for (int i = 0; i < testValues.length; i++) {
            byte[] b = ByteBuffer.allocate(8).putLong(testValues[i]).array();
            assertEquals(testValues[i], ByteArrays.parseLong(b));
        }
    }

    @Test
    public void testShift() {
        byte[] val = {(byte) 224, (byte) 1};
        byte[] shiftedVal = ByteArrays.bitshift(val, -3);
        byte[] doubleShiftedVal = ByteArrays.bitshift(shiftedVal, 3);
        assertEquals(1, doubleShiftedVal[2]);
    }

    @Test
    public void testToBitSet() {
        byte[] in = {(byte) 112, (byte) 45};

        BitSet2 expected = new BitSet2(16);
        expected.set(1, 4, true);
        expected.set(10, true);
        expected.set(12, 14, true);
        expected.set(15, true);
        assertEquals(expected, BitSet2.valueOf(in));
    }

    @Test
    public void testFromBitSet() {
        byte[] expected = {(byte) 112, (byte) 45};

        BitSet2 in = new BitSet2(16);
        in.set(1, 4, true);
        in.set(10, true);
        in.set(12, 14, true);
        in.set(15, true);
        assertTrue(Arrays.equals(expected, ByteArrays.fromBitSet(in)));
    }

}
