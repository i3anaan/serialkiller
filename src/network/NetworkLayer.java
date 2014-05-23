package network;

import common.Layer;

import javax.naming.SizeLimitExceededException;

/**
 * Abstract class for network layers. Requires two methods to be implemented
 * that are responsible for communication with upper layers.
 */
public abstract class NetworkLayer extends Layer {
    /**
     * Blocking method that returns the next available payload for an upper
     * layer.
     * @return The next available payload.
     */
    public abstract Payload read();

    /**
     * Sends the given payload over the network.
     * @param p The payload.
     * @throws SizeLimitExceededException
     */
    public abstract void send(Payload p) throws SizeLimitExceededException;
}
