package network;

/**
 * Represents a host in the network.
 */
public class Host {
    private byte address;
    private String ip;
    private Host linkedHost;
    private Handler handler;

    public Host(byte address) {
        this.address = address;
    }

    public Host(byte address, String ip) {
        this.address = address;
        this.ip = ip;
    }

    public byte address() {
        return address;
    }

    public String IP() {
        return ip;
    }

    public Host routeThrough() {
        return linkedHost;
    }

    public void routeThrough(Host linkedHost) {
        this.linkedHost = linkedHost;
    }

    public Handler handler() {
        return handler;
    }

    public void handler(Handler handler) {
        this.handler = handler;
    }

    public void clear() {
        ip = null;
        linkedHost = null;
        handler = null;
    }
}
