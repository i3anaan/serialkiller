package network.tpp.handlers;

import link.FrameLinkLayer;
import link.PacketFrameLinkLayer;
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

    /**
     * Checks whether the link layer is a PacketFrameLinkLayer.
     * @return Whether the link layer is a PacketFrameLinkLayer.
     */
    public boolean isPacketFrameLinkLayer() {
        return link instanceof PacketFrameLinkLayer;
    }

    /**
     * Casts the link layer to PacketFrameLinkLayer if possible. Otherwise,
     * return null.
     * @return The link layer as PacketFrameLinkLayer or null
     */
    protected PacketFrameLinkLayer toPacketFrameLinkLayer() {
        if (isPacketFrameLinkLayer()) {
            return (PacketFrameLinkLayer) link;
        } else {
            return null;
        }
    }
}
