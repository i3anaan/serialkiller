package network.handlers;

import link.FrameLinkLayer;
import network.NetworkLayer;

/**
 * Handler for communication with the link layer.
 */
public abstract class LinkLayerHandler extends Handler {
    FrameLinkLayer link;

    public LinkLayerHandler(NetworkLayer parent, FrameLinkLayer link) {
        super(parent);
        this.link = link;
    }
}
