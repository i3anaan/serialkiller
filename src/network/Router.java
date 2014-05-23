package network;

import java.util.*;

/**
 * Router class that keeps track of the hosts in the network and their routes.
 */
public class Router {
    Map<Byte, Host> hosts;
    Map<Host, Byte> routesCache;

    public Router() {
        hosts = new HashMap<Byte, Host>();
        routesCache = new HashMap<Host, Byte>();
    }

    /**
     * Add a host to the routes.
     * @param h The host.
     */
    public void add(Host h) {
        hosts.put(h.address(), h);
    }

    /**
     * Get the collection of hosts this router knows about.
     * @return The collection of hosts.
     */
    public Collection<Host> hosts() {
        return hosts.values();
    }

    /**
     * Gets the handler that can process the given packet. Returns null if the
     * packet is unroutable.
     * @param p The packet.
     * @return The handler the packet should be send to or null if the packet is
     *         unroutable.
     */
    public Host route(Packet p) {
        Host destination = hosts.get(p.header().getDestination());

        if (destination != null) {
            while (destination.routeThrough() != null) {
                return destination.routeThrough();
            }
        }

        return null;
    }

    /**
     * Parses the routes in a routing table to the format used by the Router.
     * This method does not link any handlers, that is the responsibility of
     * the NetworkLayer class.
     * @param t The routing table.
     */
    public void parse(RoutingTable t) {
        Set<Byte> addrSet = new HashSet<Byte>();
        addrSet.addAll(t.getRoutes().keySet());
        addrSet.addAll(t.getRoutes().values());
        addrSet.addAll(t.getTunnels().keySet());

        // Fetch all hosts
        for (Byte addr : addrSet) {
            if (addr != null && !hosts.containsKey(addr)) {
                Host host;
                if (t.getTunnels().containsKey(addr)) {
                    // Add host with IP.
                    host = new Host(addr, t.getTunnels().get(addr));
                } else if (t.getRoutes().containsKey(addr)) {
                    // Or with a route.
                    host = new Host(addr);
                    routesCache.put(host, t.getRoutes().get(addr));
                } else {
                    // Well, this host has to appear somewhere...
                    host = new Host(addr);
                }

                add(host);
            }
        }
    }

    /**
     * Updates the routes in the router. Should be run after changing Host
     * objects and/or adding hosts to the router. This method is not called
     * automatically.
     */
    public void update() {
        for (Host endHost : hosts.values()) {
            Host linkHost = null;

            // Get the route
            if (routesCache.containsKey(endHost)) {
                linkHost = hosts.get(routesCache.get(endHost));
            }

            if (endHost != linkHost) {
                endHost.routeThrough(linkHost);
            }
        }
    }

    /**
     * Clears all routes. The Host objects are cleared of everything but their
     * address and are kept available through the hosts() method.
     */
    public void clear() {
        routesCache.clear();

        for (Host host : hosts.values()) {
            host.clear();
        }
    }
}
