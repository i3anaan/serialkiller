package test;

import static org.junit.Assert.*;

import org.junit.Test;

import phys.HardwareLayer;

/**
 * This class tests the shuftling functions in the HardwareLayer class.
 */
public class ShuftleTest {

	@Test
	public void testShuftRight() {
		assertEquals(0, HardwareLayer.shuftRight((byte) 00));
		assertEquals(1, HardwareLayer.shuftRight((byte) 32));
		assertEquals(2, HardwareLayer.shuftRight((byte) 16));
		assertEquals(3, HardwareLayer.shuftRight((byte) 48));
	}
	
	@Test
	public void testShuftLeft() {
		assertEquals(00, HardwareLayer.shuftLeft((byte) 0));
		assertEquals(32, HardwareLayer.shuftLeft((byte) 1));
		assertEquals(16, HardwareLayer.shuftLeft((byte) 2));
		assertEquals(48, HardwareLayer.shuftLeft((byte) 3));
		assertEquals(00, HardwareLayer.shuftLeft((byte) 4));
	}

}
