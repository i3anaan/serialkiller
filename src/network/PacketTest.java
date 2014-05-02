package network;

import junit.framework.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;

/** Unit tests for the Packet class. */
public class PacketTest extends TestCase {
    Packet p;

    public void setUp() {
        p = new Packet(0);
    }

    @Test
    public void testFromRaw() {
        // Generate random data.
        byte[] raw = new byte[1024];
        new Random().nextBytes(raw);

        // Create packet.
        p = new Packet(raw);

        assertEquals("Packet payload size incorrect.", 1024 - Packet.HEADER_LENGTH, p.payload().length);
        assertTrue("Header data incorrect.", Arrays.equals(Arrays.copyOfRange(raw, 0, Packet.HEADER_LENGTH), p.header().compile()));
        assertTrue("Precompiled flag not set.", p.isPrecompiled());
        assertTrue("Compilated output not equal to input.", Arrays.equals(raw, p.compile()));
    }

    @Test
    public void testPayload() {
        // Generate random data.
        byte[] data = new byte[879];
        new Random().nextBytes(data);

        // Set the payload.
        p.setPayload(data);

        assertEquals("Payload length set incorrectly.", 879, p.header().getLength());
        assertTrue("Payload data not equal to input.", Arrays.equals(data, p.payload()));
    }

}
