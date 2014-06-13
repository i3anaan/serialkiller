package network.tpp.handlers;

import network.tpp.Packet;
import network.tpp.TPPNetworkLayer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Moves packets between queues to ensure a timeout for trying to re-offer
 * packets in the main queue.
 */
public class ReofferHandler extends Handler {
    TPPNetworkLayer parent;
    ConcurrentHashMap<Byte, Packet> nextPackets;

    public ReofferHandler(TPPNetworkLayer parent) {
        super(parent);
        this.parent = parent;
        nextPackets = new ConcurrentHashMap<Byte, Packet>();
    }

    @Override
    public void handle() throws InterruptedException {
        Thread.sleep(TPPNetworkLayer.TIMEOUT);

        Packet p = out.take();

        if (nextPackets.get(p.header().getDestination()) == null) {
            // Set packet as the next packet for a host.
            nextPackets.put(p.header().getDestination(), p);
        } else {
            // Delay processing.
            out.put(p);
        }
    }

    public void notify(Byte address) {
        if (nextPackets.get(address) != null) {
            parent.sendPacket(nextPackets.get(address));
        }
    }

    public String toString() {
        return "Reoffer" + super.toString();
    }
}
