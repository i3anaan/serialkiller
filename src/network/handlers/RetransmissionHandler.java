package network.handlers;

import network.NetworkLayer;
import network.Packet;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Makes sure that packets are retransmitted.
 */
public class RetransmissionHandler extends Handler {
    private String name = "RetransmissionHandler";

    NetworkLayer parent;

    public RetransmissionHandler(NetworkLayer parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void handle() throws InterruptedException {
        // Make sure the queue is filled by checking for retransmissions
        parent.checkRetransmissions();

        Packet p = out.poll(NetworkLayer.TIMEOUT / 10, TimeUnit.MILLISECONDS);

        while (p != null) {
            // Offer again
            in.add(p);

            // Mark packet as sent
            parent.markSent(p);

            p = out.take();
        }
    }
}
