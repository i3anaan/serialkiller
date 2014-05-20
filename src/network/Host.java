package network;

import network.handlers.Handler;

/**
 * Represents a host in the network.
 */
public class Host {
    /** The TPP address of the host. Required. */
    private byte address;

    /** The IP address of the host. Can be null. A present IP address will
     * result in a tunnel to the host. */
    private String ip;

    /** The host that we need to route through to reach this host. Can be null,
     * in that case the host is directly reachable. */
    private Host linkedHost;

    /** The handler for this host. Can be null, in that case we cannot
     * (directly) handle packets with this host as destination. If no linkedHost
     * is set, this host is not reachable.
     */
    private Handler handler;

    /**
     * Creates a new Host instance.
     * @param address The TPP address of the host.
     */
    public Host(byte address) {
        this.address = address;
    }

    /**
     * Creates a new Host instance with a tunnel.
     * @param address The TPP address of the host.
     * @param ip The IP address of this host.
     */
    public Host(byte address, String ip) {
        this.address = address;
        this.ip = ip;
    }

    /**
     * Returns the TPP address of this host.
     * @return The TPP address of this host.
     */
    public byte address() {
        return address;
    }

    /**
     * Returns the IP address of this host.
     * @return The IP address of this host.
     */
    public String IP() {
        return ip;
    }

    /**
     * Returns a Host instance through which this host is reachable, or null if
     * there is none.
     * @return A Host instance through which this host is reachable or null
     */
    public Host routeThrough() {
        return linkedHost;
    }

    /**
     * Sets the Host instance through which this host is reachable.
     * @param linkedHost The Host instance through which this host is reachable.
     *                   Should be null if there is none.
     */
    public void routeThrough(Host linkedHost) {
        this.linkedHost = linkedHost;
    }

    /**
     * Returns the Handler subclass instance that is used to handle packets for
     * this host. May be null if there is none.
     * @return The Handler subclass instance that is used to handle packets for
     *         this host, or null.
     */
    public Handler handler() {
        return handler;
    }

    /**
     * Sets the Handler subclass instance that is used to handle packets for
     * this host. May be null if there is none.
     * @param handler The Handler subclass instance that is used to handle
     *                packets for this host, or null.
     */
    public void handler(Handler handler) {
        this.handler = handler;
    }

    /**
     * Clears the routing and packet handling settings for this host. The TPP
     * address is kept.
     */
    public void clear() {
        ip = null;
        linkedHost = null;
        handler = null;
    }

    public String toString() {
        return String.format("Host<%d>", address);
    }
}
