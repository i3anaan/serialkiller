package network;

/**
 * Simple class designed to enable returning two specific types at once, from
 * the network layer to the application layer.
 */
public class Payload {
    public byte[] data;
    public byte address;

    public Payload(byte[] data, byte address) {
        this.data = data;
        this.address = address;
    }

    /**
     * Clones the current instance into a new Payload instance with the same
     * data and address.
     * @return The new Payload instance.
     */
    public Payload clone() {
        return new Payload(data, address);
    }
}
