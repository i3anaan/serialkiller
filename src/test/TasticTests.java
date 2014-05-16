package test;

import static org.junit.Assert.*;
import link.BittasticLinkLayer;

import org.junit.Test;

public class TasticTests {

	@Test
	public void testPackUnpack() {
		assertEquals(42, BittasticLinkLayer.unpackPair(BittasticLinkLayer.packPair((byte) 42)));
		assertTrue(BittasticLinkLayer.validPair(BittasticLinkLayer.packPair((byte) 42)));
	}

}
