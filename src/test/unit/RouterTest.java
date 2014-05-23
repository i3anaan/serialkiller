package test.unit;

import junit.framework.TestCase;
import network.Host;
import network.Packet;
import network.Router;
import network.RoutingTable;
import org.junit.Test;

import java.io.IOException;


public class RouterTest extends TestCase {
    Router r;

    public void setUp() {
        r = new Router();
    }

    @Test
    public void testParse() throws IOException {
        RoutingTable t = new RoutingTable("src/test/routes.txt");
        r.parse(t);
        r.update();

        assertEquals("The number of hosts is not correct.", 5, r.hosts().size());

        for (Host h : r.hosts()) {
            if (h.address() == 1) {
                assertEquals("Host 1 routes incorrectly.", 2, h.routeThrough().address());
                assertEquals("Host 1 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 2) {
                assertEquals("Host 2 routes incorrectly.", null, h.routeThrough());
                assertEquals("Host 2 has an incorrect IP.", "192.168.0.1", h.IP());
            } else if (h.address() == 3) {
                assertEquals("Host 3 routes incorrectly.", 2, h.routeThrough().address());
                assertEquals("Host 3 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 5) {
                assertEquals("Host 5 routes incorrectly.", 8, h.routeThrough().address());
                assertEquals("Host 5 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 8) {
                assertEquals("Host 8 routes incorrectly.", null, h.routeThrough());
            }
        }
    }

    @Test
    public void testAdd() throws IOException {
        RoutingTable t = new RoutingTable("src/test/routes.txt");
        r.parse(t);
        r.update();

        Host g = new Host((byte) 8, "8.8.8.8");
        r.add(g);
        r.update();

        assertEquals("The number of hosts is not correct.", 5, r.hosts().size());

        for (Host h : r.hosts()) {
            if (h.address() == 1) {
                assertEquals("Host 1 routes incorrectly.", 2, h.routeThrough().address());
                assertEquals("Host 1 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 2) {
                assertEquals("Host 2 routes incorrectly.", null, h.routeThrough());
                assertEquals("Host 2 has an incorrect IP.", "192.168.0.1", h.IP());
            } else if (h.address() == 3) {
                assertEquals("Host 3 routes incorrectly.", 2, h.routeThrough().address());
                assertEquals("Host 3 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 5) {
                assertEquals("Host 5 routes incorrectly.", 8, h.routeThrough().address());
                assertEquals("Host 5 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 8) {
                assertEquals("Host 8 routes incorrectly.", null, h.routeThrough());
                assertEquals("Host 8 has an incorrect IP.", "8.8.8.8", h.IP());
            }
        }
    }
}
