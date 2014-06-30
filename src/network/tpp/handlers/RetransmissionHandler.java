package network.tpp.handlers;

import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;

import java.util.concurrent.DelayQueue;

/**
 * Makes sure that packets are retransmitted.
 */
public class RetransmissionHandler extends Handler {
    DelayQueue<Packet> delayed;
    TPPNetworkLayer parent;

    public RetransmissionHandler(TPPNetworkLayer parent) {
        super(parent);
        this.parent = parent;
        this.delayed = parent.retransmissionQueue();
    }

    @Override
    public void handle() throws InterruptedException {
        // Take the next item out of the queue.
        Packet p = delayed.take();
        parent.markAsDropped(p);

        // Check if it should be dropped.
        if (p.retransmissions() < TPPNetworkLayer.MAX_RETRANSMISSIONS) {
            // Increase retransmissions
            p.retransmit();
            TPPNetworkLayer.getLogger().debug("Retransmitting " + p.toString() + ".");
            parent.sendPacket(p);
        } else {
            TPPNetworkLayer.getLogger().warning(p.toString() + String.format(" dropped after %d retransmissions.", p.retransmissions()));
        }
    }

    @Override
    public boolean offer(Packet p) {
        return delayed.offer(p);
    }

    @Override
	public String toString() {
        return "Retransmission" + super.toString();
    }
}
