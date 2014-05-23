package network.tpp;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Random;

/** Unit tests for the Packet class. */
public class PacketTest {
    Packet p;

    public void setUp() {
        p = new Packet(0);
    }

    @Test
    public void testFromRaw() {
        // The packet.
        byte[] data = {32, 125, 78, 91, 117, 62, 14, 66, 8, 11, 2, 16, 97, 58, 60, 89, 1, 66, 39, 6, 20, 34, 125, 77, 43, 1, 98, 0, 68, 94, 79, 50, 46, 47, 75, 42, 97, 10, 21, 51, 77, 52, 95, 26, 92, 97, 14, 50, 125, 105, 116, 60, 42, 37, 2, 56, 120, 124, 32, 72, 72, 55, 16, 4, 120, 65, 16, 117, 87, 28, 91, 82, 115, 88, 104, 55, 0, 87, 19, 102, 7, 41, 76, 89, 62, 80, 22, 29, 11, 102, 115, 24, 75, 24, 68, 4, 78, 89, 26, 74, 54, 65, 124, 91, 21, 78, 72, 23, 116, 76, 2, 97, 84, 70, 82, 39, 106, 124, 22, 24, 50, 104, 79, 108, 60, 120, 40, 10, 100, 75, 59, 124, 110, 38, 68, 100, 84, 40, 102, 114, 18, 113, 87, 5, 56, 49, 12, 58, 23, 113, 94, 114, 1, 9, 119, 57, 1, 64, 66, 84, 83, 114, 52, 90, 6, 13, 105, 25, 87, 0, 38, 116, 105, 12, 55, 87, 45, 97, 115, 28, 6, 125, 104, 119, 79, 67, 86, 43, 113, 2, 66, 36, 21, 23, 19, 4, 24, 67, 123, 59, 69, 84, 25, 86, 59, 91, 27, 106, 83, 72, 109, 73, 74, 90, 86, 105, 55, 43, 10, 105, 90, 12, 111, 42, 12, 108, 20, 115, 79, 117, 45, 20, 76, 113, 48, 17, 0, 69, 5, 9, 106, 81, 121, 107, 76, 103, 8, 24, 24, 30, 77, 70, 123, 55, 81, 110,};
        byte[] raw;
        Packet q = new Packet(0);
        q.setPayload(data);
        q.header().setDestination((byte) 1);
        q.header().setSender((byte) 0);
        q.header().setTTL(5);
        q.header().setMore(true);
        q.header().setSegnum(4000);
        raw = q.compile();

        // Create packet.
        p = new Packet(raw);
        byte[] compiled = p.compile();

        assertEquals("Packet payload size incorrect.", data.length, p.payload().length);
        assertTrue("Packet payload incorrect.", Arrays.equals(data, p.payload()));
        assertTrue("Precompiled flag not set.", p.isPrecompiled());
        assertTrue("Compiled packet not equal to second compilation.", Arrays.equals(p.compile(), p.compile()));
        assertTrue("Packet does not verify.", p.verify());
        assertTrue("Compiled output not equal to input.", Arrays.equals(raw, p.compile()));
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
