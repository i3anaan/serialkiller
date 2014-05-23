package network.tpp;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

/** Unit tests for the PacketHeader class. */
public class PacketHeaderTest {
    PacketHeader h;

    @Before
    public void setUp() {
        h = new PacketHeader();
    }

    /**
     * Make sure that the header is filled with zeros on the bit places
     * indicated by the start en stop arguments.
     * @param start The start of the range (inclusive).
     * @param stop The end of the range (exclusive).
     */
    public void ensureEmptyHeader(int start, int stop) {
        // Put data in a bit set.
        BitSet data = h.raw();
        assertTrue("The header is not completely empty on the specified range.", data.nextSetBit(start) >= stop || data.nextSetBit(start) == -1);
    }

    @Test
    public void testChecksum() {
        // The checksum should be empty
        assertEquals("The checksum is not zero.", h.getChecksum(), 0L);

        // Check for overflows.
        try {
            h.setChecksum(4294967296L);
            fail("The checksum can be overflowed.");
        } catch (IllegalArgumentException e) {
            // Expected, do nothing.
        }

        // Set a real checksum.
        h.setChecksum(2147483648L);

        // Check that the right checksum is set.
        assertEquals("The checksum is not read/set correctly", 2147483648L, h.getChecksum());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(32, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testTTL() {
        // The TTL should be empty
        assertEquals("The TTL is not zero.", 0, h.getTTL());

        // Check for overflows.
        try {
            h.setTTL(PacketHeader.MAX_TTL + 1);
            fail("The TTL can be overflowed.");
        } catch (IllegalArgumentException e) {
            // Expected, do nothing.
        }

        // Set a real TTL.
        h.setTTL(6);

        // Check that the right TTL is set.
        assertEquals("The TTL is not read/set correctly.", 6, h.getTTL());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 32);
        ensureEmptyHeader(35, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testAck() {
        // The Ack flag should be off.
        assertEquals("The Ack flag is not false.", false, h.getAck());

        // Set the Ack flag.
        h.setAck(true);

        // Check that the right flag is set.
        assertEquals("The Ack flag is not read/set correctly.", true, h.getAck());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 35);
        ensureEmptyHeader(36, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testMore() {
        // The More flag should be off.
        assertEquals("The More flag is not false.", false, h.getMore());

        // Set the More flag.
        h.setMore(true);

        // Check that the right flag is set.
        assertEquals("The More flag not read/set correctly.", true, h.getMore());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 36);
        ensureEmptyHeader(37, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testLength() {
        // The length should be zero.
        assertEquals("The length is not zero.", 0L, h.getLength());

        // Check for overflows.
        try {
            h.setLength(1025L);
            fail("The length can be overflowed.");
        } catch (IllegalArgumentException e) {
            // Expected, do nothing.
        }

        // Set a real length.
        h.setLength(456L);

        // Check that the right length is set.
        assertEquals("The length is not read/set correctly.", 456L, h.getLength());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 37);
        ensureEmptyHeader(48, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testSender() {
        // The sender should be zero.
        assertEquals("The sender is not zero", 0, h.getSender());

        // Set a sender.
        h.setSender((byte) 89);

        // Check that the right sender is set.
        assertEquals("The sender is not read/set correctly.", 89, h.getSender());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 48);
        ensureEmptyHeader(56, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testDestination() {
        // The destination should be zero.
        assertEquals("The destination is not zero", 0, h.getDestination());

        // Set a destination.
        h.setDestination((byte) 202);

        // Check that the right destination is set.
        assertEquals("The destination is not read/set correctly.", (byte) 202, h.getDestination());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 56);
        ensureEmptyHeader(64, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testSeqnum() {
        // The sequence number should be zero.
        assertEquals("The sequence number is not zero.", 0, h.getSeqnum());

        // Check for overflows.
        try {
            h.setSeqnum(256);
            fail("The sequence number can be overflowed.");
        } catch (IllegalArgumentException e) {
            // Expected, do nothing.
        }

        // Set a real sequence number.
        h.setSeqnum(42);

        // Check that the right sequence number is set.
        assertEquals("The sequence number is not read/set correctly.", 42, h.getSeqnum());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 64);
        ensureEmptyHeader(72, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testAcknum() {
        // The acknowledgement number should be zero.
        assertEquals("The acknowledgement number is not zero.", 0, h.getAcknum());

        // Check for overflows.
        try {
            h.setAcknum(256);
            fail("The acknowledgement number can be overflowed.");
        } catch (IllegalArgumentException e) {
            // Expected, do nothing.
        }

        // Set a real acknowledgement number.
        h.setAcknum(42);

        // Check that the right acknowledgement number is set.
        assertEquals("The acknowledgement number is not read/set correctly.", 42, h.getAcknum());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 72);
        ensureEmptyHeader(80, PacketHeader.HEADER_LENGTH * 8);
    }

    @Test
    public void testSegnum() {
        // The segment number should be zero.
        assertEquals("The segment number is not zero.", 0, h.getSegnum());

        // Check for overflows.
        try {
            h.setSegnum(PacketHeader.MAX_SEGNUM + 1);
            fail("The segment number can be overflowed.");
        } catch (IllegalArgumentException e) {
            // Expected, do nothing.
        }

        // Set a real segment number.
        h.setSegnum(42);

        // Check that the right segment number is set.
        assertEquals("The segment number is not read/set correctly.", 42, h.getSegnum());

        // Check that the rest of the header is untouched.
        ensureEmptyHeader(0, 80);
    }

    @Test
    public void testPrecompiled() {
        // The empty header should not be precompiled
        assertFalse(h.precompiled);

        // A header constructed from raw data should be precompiled...
        h = new PacketHeader(new byte[PacketHeader.HEADER_LENGTH]);
        assertTrue(h.precompiled);

        // ... but not when it has changed.
        h.setLength(512);
        assertFalse(h.precompiled);
    }

    @Test
    public void testEmpty() {
        // We do not really expect anything but an empty array.
        byte[] expected = new byte[PacketHeader.HEADER_LENGTH];

        // Check if the result equals the expectation.
        assertTrue("Compiling an empty header failed.", Arrays.equals(expected, h.compile()));
    }

    @Test
    public void testFromRaw() {
        // We take some random data as an example.
        byte[] raw = new byte[PacketHeader.HEADER_LENGTH];
        new Random().nextBytes(raw);
        byte[] expected = raw;

        // Set it to the header
        h = new PacketHeader(raw);

        // Check if the result equals the expectation.
        assertTrue("Compiling a precompiled header failed.", Arrays.equals(expected, h.compile()));
    }

}
