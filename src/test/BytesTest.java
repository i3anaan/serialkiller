package test;

import static org.junit.Assert.*;

import org.junit.Test;

import util.Bytes;

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

}
