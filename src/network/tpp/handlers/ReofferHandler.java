package network.tpp.handlers;

import network.tpp.Packet;
import network.tpp.TPPNetworkLayer;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Moves packets between queues to ensure a timeout for trying to re-offer
 * packets in the main queue.
 */
public class ReofferHandler extends Handler {
    TPPNetworkLayer parent;
    ConcurrentHashMap<Byte, LinkedList<Packet>> nextPackets;

    public ReofferHandler(TPPNetworkLayer parent) {
        super(parent);
        this.parent = parent;
        nextPackets = new ConcurrentHashMap<Byte, LinkedList<Packet>>();
    }

    @Override
    public void handle() throws InterruptedException {
        // Not used.
    }

    @Override
    public void run() {
        // Do nothing.
    }

    @Override
    public boolean offer(Packet p) {
        byte address = p.header().getDestination();

        // Create key if necessary.
        if (nextPackets.get(address) == null) {
            nextPackets.put(address, new LinkedList<Packet>());
        }

        // Add packet to the end of the list for this host.
        nextPackets.get(address).addLast(p);

        return true;
    }


    public void notify(Byte address) {
        if (nextPackets.containsKey(address) && !nextPackets.get(address).isEmpty()) {
            Packet p = nextPackets.get(address).removeFirst();
            parent.sendPacket(p);
        }
    }

    public String toString() {
        return "Reoffer" + super.toString();
    }
}
