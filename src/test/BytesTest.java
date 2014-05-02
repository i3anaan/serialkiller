package test;

import static org.junit.Assert.*;

import org.junit.Test;

import util.Bytes;

import java.util.Arrays;
import util.BitSet2;

/** Unit tests for the Bytes class. */
public class BytesTest {

	@Test
	public void testFormat() {
		assertEquals("11111110", Bytes.format((byte) -2));
		assertEquals("11111111", Bytes.format((byte) -1));
		assertEquals("00000000", Bytes.format((byte) 0));
		assertEquals("00000001", Bytes.format((byte) 1));
		assertEquals("00000010", Bytes.format((byte) 2));
		assertEquals("00000011", Bytes.format((byte) 3));
		assertEquals("01111111", Bytes.format((byte) 127));
	}

    @Test
    public void testParseBoolean() {
        assertEquals(true, Bytes.parseBoolean((byte) 2, 6));
        assertEquals(true, Bytes.parseBoolean((byte) 1, 7));
        assertEquals(false, Bytes.parseBoolean((byte) 4, 7));
        assertEquals(true, Bytes.parseBoolean((byte) -1, 5));
    }

    @Test
    public void testFromBitSet() {
        byte expected = (byte) 6;

        BitSet2 in = new BitSet2(16);
        in.set(9, 11, true);

        assertEquals(expected, Bytes.fromBitSet(in, 4));
    }

}
