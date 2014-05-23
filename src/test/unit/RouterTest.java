package test.unit;

import network.tpp.Host;
import network.tpp.Router;
import network.tpp.RoutingTable;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;


public class RouterTest {
    Router r;

    @Before
    public void setUp() {
        r = new Router();
    }

    @Test
    public void testParse() throws IOException {
        RoutingTable t = new RoutingTable("src/test/routes.txt");
        r.parse(t);
        r.update();

        assertEquals("The number of hosts is not correct.", 6, r.hosts().size());

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
                assertEquals("Host 5 routes incorrectly.", 3, h.routeThrough().address());
                assertEquals("Host 5 has an incorrect IP.", null, h.IP());
            }
        }

        assertEquals("Self address incorrect.", 6, r.self());
        assertEquals("Sibling address incorrect.", 7, r.sibling());
    }

    @Test
    public void testAdd() throws IOException {
        RoutingTable t = new RoutingTable("src/test/routes.txt");
        r.parse(t);
        r.update();

        Host g = new Host((byte) 4, "8.8.8.8");
        r.add(g);
        r.update();

        assertEquals("The number of hosts is not correct.", 7, r.hosts().size());

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
            } else if (h.address() == 4) {
                assertEquals("Host 4 routes incorrectly.", null, h.routeThrough());
                assertEquals("Host 4 has an incorrect IP.", "8.8.8.8", h.IP());
            } else if (h.address() == 5) {
                assertEquals("Host 5 routes incorrectly.", 3, h.routeThrough().address());
                assertEquals("Host 5 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 6) {
                assertEquals("Host 6 routes incorrectly.", null, h.routeThrough());
                assertEquals("Host 6 has an incorrect IP.", null, h.IP());
            } else if (h.address() == 7) {
                assertEquals("Host 7 routes incorrectly.", null, h.routeThrough());
                assertEquals("Host 7 has an incorrect IP.", null, h.IP());
            }
        }
    }
}
