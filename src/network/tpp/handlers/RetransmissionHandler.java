package network.tpp.handlers;

import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;

/**
 * Makes sure that packets are retransmitted.
 */
public class RetransmissionHandler extends Handler {
    DelayQueue<Packet> out = new DelayQueue<Packet>();

    public RetransmissionHandler(TPPNetworkLayer parent) {
        super(parent);
        this.out = parent.retransmissionQueue();
    }

    @Override
    public void handle() throws InterruptedException {
        // Take the next item out of the queue.
        Packet p = out.take();

        // Check if it should be dropped.
        if (p.retransmissions() < TPPNetworkLayer.MAX_RETRANSMISSIONS) {
            // Increase retransmissions
            p.retransmit();
            in.put(p);
        }
    }

    public String toString() {
        return "Retransmission" + super.toString();
    }
}
