package test;

import static org.junit.Assert.*;
import link.StatefulHDXLinkLayer;

import org.junit.Test;

/**
 * Tests the encode/decode functions of StatefulHDXLinkLayer.
 */
public class SHDXLLTest {

	@Test
	public void testEncode() {
		assertEquals(0, StatefulHDXLinkLayer.encode(0, StatefulHDXLinkLayer.NO_CHANGE));
		assertEquals(1, StatefulHDXLinkLayer.encode(0, StatefulHDXLinkLayer.ZERO_BIT_ACK));
		assertEquals(2, StatefulHDXLinkLayer.encode(0, StatefulHDXLinkLayer.ONE_BIT_ACK));
		assertEquals(3, StatefulHDXLinkLayer.encode(0, StatefulHDXLinkLayer.NO_BIT_ACK));
		
		assertEquals(3, StatefulHDXLinkLayer.encode(3, StatefulHDXLinkLayer.NO_CHANGE));
		assertEquals(0, StatefulHDXLinkLayer.encode(3, StatefulHDXLinkLayer.ZERO_BIT_ACK));
		assertEquals(1, StatefulHDXLinkLayer.encode(3, StatefulHDXLinkLayer.ONE_BIT_ACK));
		assertEquals(2, StatefulHDXLinkLayer.encode(3, StatefulHDXLinkLayer.NO_BIT_ACK));
	}
	
	@Test
	public void testDecode() {
		assertEquals(StatefulHDXLinkLayer.NO_CHANGE, StatefulHDXLinkLayer.decode(3, 3));
		assertEquals(StatefulHDXLinkLayer.ZERO_BIT_ACK, StatefulHDXLinkLayer.decode(3, 0));
		assertEquals(StatefulHDXLinkLayer.ONE_BIT_ACK, StatefulHDXLinkLayer.decode(3, 1));
		assertEquals(StatefulHDXLinkLayer.NO_BIT_ACK, StatefulHDXLinkLayer.decode(3, 2));
	}

}
