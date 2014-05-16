package network.handlers;

import link.FrameLinkLayer;
import network.NetworkLayer;
import network.Packet;

/**
 * Handler for data to the link layer.
 */
public class LinkLayerOutHandler extends LinkLayerHandler {
    private String name = "LinkHandler<Out>";

    public LinkLayerOutHandler(NetworkLayer parent, FrameLinkLayer link) {
        super(parent, link);
    }

    @Override
    public void handle() {
        try {
            Packet p = out.take();
            link.sendFrame(p.compile());
        } catch (InterruptedException e) {
            // TODO: Log
            this.stop();
        }
    }
}
