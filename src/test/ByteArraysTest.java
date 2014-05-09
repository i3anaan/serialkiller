package test;

import static org.junit.Assert.*;

import org.junit.Test;

import util.ByteArrays;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

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
        byte[] in = {(byte) 112, (byte) 45, (byte) 0};

        BitSet expected = new BitSet(24);
        expected.set(1+2, 4+2, true);
        expected.set(10+2, true);
        expected.set(12+2, 14+2, true);
        expected.set(15+2, true);

        assertEquals(expected, ByteArrays.toBitSet(in, 24, 2));
    }

    @Test
    public void testFromBitSet() {
        byte[] expected = {(byte) 112, (byte) 45, (byte) 0};

        BitSet in = new BitSet(16);
        in.set(1, 4, true);
        in.set(10, true);
        in.set(12, 14, true);
        in.set(15, true);

        assertTrue(Arrays.equals(expected, ByteArrays.fromBitSet(in, 3)));
    }

}
