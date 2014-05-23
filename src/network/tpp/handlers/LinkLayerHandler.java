package network.tpp.handlers;

import link.FrameLinkLayer;
import network.tpp.TPPNetworkLayer;

/**
 * Handler for communication with the link layer.
 */
public abstract class LinkLayerHandler extends Handler {
    FrameLinkLayer link;

    public LinkLayerHandler(TPPNetworkLayer parent, FrameLinkLayer link) {
        super(parent);
        this.link = link;
    }
}
