package test.unit;

import static org.junit.Assert.*;
import java.io.IOException;
import java.net.URL;

import network.RoutingTable;
import org.junit.Test;

public class RoutingTableTest {
	@Test
	public void testParse() throws IOException {
		RoutingTable rt = new RoutingTable("src/test/routes.txt");
		assertEquals(3, rt.getRoutes().size());
		assertEquals(1, rt.getTunnels().size());
	}
	
	@Test
	public void testToGraph() throws IOException {
		RoutingTable rt = new RoutingTable("src/test/routes.txt");
		assertNotNull(rt.toGraph());
		assertNotEquals("", rt.toGraph());
	}
	
	@Test
	public void testToGraphUri() throws IOException {
		RoutingTable rt = new RoutingTable("src/test/routes.txt");
		assertNotNull(rt.toGraphUri());
		assertNotEquals("", rt.toGraphUri());
		assertEquals(0x89, new URL(rt.toGraphUri()).openConnection().getInputStream().read());
	}
}
