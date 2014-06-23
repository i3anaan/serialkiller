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
    // TODO Make this another way so no busy waiting occurs
    public void handle() throws InterruptedException {
        Packet p = out.peek();

        if (p != null && nextPackets.get(p.header().getDestination()) == null) {
            if (parent.getCongestion(p.header().getDestination()) < TPPNetworkLayer.MAX_FOR_HOST) {
                parent.sendPacket(p);
            } else {
                // Set packet as the next packet for a host.
                nextPackets.put(p.header().getDestination(), p);
                // Remove element from queue
                out.take();
            }
        } else {
            Thread.sleep(TPPNetworkLayer.TIMEOUT / 4);
        }
    }

    public void notify(Byte address) {
        if (nextPackets.get(address) != null) {
            parent.sendPacket(nextPackets.get(address));
            nextPackets.remove(address);
        }
    }

    public String toString() {
        return "Reoffer" + super.toString();
    }
}
