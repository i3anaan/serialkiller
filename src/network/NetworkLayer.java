package network;

import java.util.ArrayList;
import java.util.Collection;

import common.Layer;
import common.Startable;

import javax.naming.SizeLimitExceededException;

import network.tpp.Host;

/**
 * Abstract class for network layers. Requires two methods to be implemented
 * that are responsible for communication with upper layers.
 */
public abstract class NetworkLayer extends Layer implements Startable {
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
    
    /**
     * Returns a collection of all addresses known to the router.
     * @return The collection of all known addresses.
     */
    public abstract Collection<Byte> hosts();
}
