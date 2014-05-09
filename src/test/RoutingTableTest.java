package test;

import static org.junit.Assert.*;
import junit.framework.TestCase;
import network.NoSuchAddressException;
import network.RoutingTable;
import org.junit.Test;

public class RoutingTableTest extends TestCase {
	private RoutingTable rt;
	
	@Override
	protected void setUp() throws Exception {
		rt = new RoutingTable();
		rt.fromString("10 1\n20 2");
	}
	
	@Test
	public void testRead() throws Exception {
		
		assertEquals(1, rt.getInterface(10));
		assertEquals(2, rt.getInterface(20));
	}
	
	@Test
	public void testUnknownAddress() throws Exception {
		try {
			rt.getInterface(100);
			fail("NoSuchAddress somehow not thrown?");
		} catch (NoSuchAddressException e) {
		}
	}

}
