package network;

import link.LinkLayer;

/**
 * Handler for data to the link layer.
 */
public class LinkLayerOutHandler extends LinkLayerHandler {
    public LinkLayerOutHandler(NetworkLayer parent, LinkLayer link) {
        super(parent, link);
    }

    @Override
    public void handle() {
        try {
            Packet p = out.take();
            link.sendFrame(p.compile()); // TODO: This method is not in the API yet.
        } catch (InterruptedException e) {
            // TODO: Handle exeption
        }
    }
}
