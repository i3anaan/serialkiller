package network.tpp.handlers;

import network.tpp.Packet;
import network.tpp.TPPNetworkLayer;

import java.util.concurrent.TimeUnit;

/**
 * Moves packets between queues to ensure a timeout for trying to re-offer
 * packets in the main queue.
 */
public class ReofferHandler extends Handler {
    TPPNetworkLayer parent;

    public ReofferHandler(TPPNetworkLayer parent) {
        super(parent);
        this.parent = parent;
    }

    @Override
    public void handle() throws InterruptedException {
        Thread.sleep(TPPNetworkLayer.TIMEOUT / 2);

        Packet p = out.take();
        parent.sendPacket(p);
    }

    public String toString() {
        return "Reoffer" + super.toString();
    }
}
