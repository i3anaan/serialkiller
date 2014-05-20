package network;

/**
 * Simple class to enable returning two types at once.
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
