package test.unit;

import static org.junit.Assert.*;
import link.BittasticLinkLayer;

import org.junit.Test;

public class TasticTests {

	@Test
	public void testPackUnpack() {
		assertEquals(42, BittasticLinkLayer.unpackPair(BittasticLinkLayer.packPair((byte) 42, false)));
		assertTrue(BittasticLinkLayer.isValidPair(BittasticLinkLayer.packPair((byte) 42, false)));
	}

}
