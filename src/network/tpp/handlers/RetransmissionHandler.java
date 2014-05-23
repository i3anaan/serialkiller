package network.tpp.handlers;

import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;

import java.util.concurrent.TimeUnit;

/**
 * Makes sure that packets are retransmitted.
 */
public class RetransmissionHandler extends Handler {
    TPPNetworkLayer parent;

    public RetransmissionHandler(TPPNetworkLayer parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void handle() throws InterruptedException {
        // Make sure the queue is filled by checking for retransmissions.
        parent.checkRetransmissions();

        Packet p = out.poll(TPPNetworkLayer.TIMEOUT / 2, TimeUnit.MILLISECONDS);

        while (p != null) {
            // Offer again.
            in.put(p);

            // Mark packet as sent.
            parent.markSent(p);

            p = out.take();
        }
    }

    public String toString() {
        return "Retransmission" + super.toString();
    }
}
