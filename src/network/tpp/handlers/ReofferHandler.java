package network.tpp.handlers;

import network.tpp.Packet;
import network.tpp.TPPNetworkLayer;

import java.util.concurrent.TimeUnit;

/**
 * Moves packets between queues to ensure a timeout for trying to re-offer
 * packets in the main queue.
 */
public class ReofferHandler extends Handler {
    public ReofferHandler(TPPNetworkLayer parent) {
        super(parent);
    }

    @Override
    public void handle() throws InterruptedException {
        Thread.sleep(TPPNetworkLayer.TIMEOUT / 2);

        Packet p = out.take();

        while (p != null) {
            in.put(p);
            p = out.poll();
        }
    }

    public String toString() {
        return "Reoffer" + super.toString();
    }
}
