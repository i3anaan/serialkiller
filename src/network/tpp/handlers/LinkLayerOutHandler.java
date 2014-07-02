package network.tpp.handlers;

import link.FrameLinkLayer;
import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;

/**
 * Handler for data to the link layer.
 */
public class LinkLayerOutHandler extends LinkLayerHandler {
    public LinkLayerOutHandler(TPPNetworkLayer parent, FrameLinkLayer link) {
        super(parent, link);
    }

    @Override
    public void handle() throws InterruptedException {
        Packet p = out.take();
        link.sendFrame(p.compile());
    }

    @Override
    public boolean offerWithPriority(Packet p) {
        link.sendFrame(p.compile());
        return true;
    }

    @Override
	public String toString() {
        return "LinkLayerOut" + super.toString();
    }
}
