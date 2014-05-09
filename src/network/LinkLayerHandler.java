package network;

import link.LinkLayer;

/**
 * Handler for communication with the link layer.
 */
public abstract class LinkLayerHandler extends Handler {
    LinkLayer link;

    public LinkLayerHandler(NetworkLayer parent, LinkLayer link) {
        super(parent);
        this.link = link;
    }
}
