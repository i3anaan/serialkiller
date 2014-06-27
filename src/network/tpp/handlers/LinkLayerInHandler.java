package network.tpp.handlers;

import link.FrameLinkLayer;
import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;

import java.util.Arrays;

/**
 * Handler for data from the link layer.
 *
 * Checks whether a FrameLinkLayer or PacketFrameLinkLayer is used. Any
 * LinkLayer implementation that uses FrameLinkLayer (and not
 * PacketFrameLinkLayer) should work with a whole packet as a frame. If not, it
 * should implement PacketFrameLinkLayer.
 */
public class LinkLayerInHandler extends LinkLayerHandler {
    public LinkLayerInHandler(TPPNetworkLayer parent, FrameLinkLayer link) {
        super(parent, link);
    }

    @Override
    public void handle() {
        byte[] data;

        // Get new data frame from the link.
        if (isPacketFrameLinkLayer()) {
            data = this.toPacketFrameLinkLayer().readPacket();
        } else {
            data = link.readFrame();
        }

        // Make sure the data fits in a packet, otherwise crop it.
        if (data.length > Packet.MAX_PACKET_LENGTH) {
            data = Arrays.copyOfRange(data, 0, Packet.MAX_PACKET_LENGTH);
            TPPNetworkLayer.getLogger().warning("Link layer delivered a packet that is too long, packet cropped.");
        }

        // Reconstruct the packet.
        Packet p = new Packet(data);

        // Verify the integrity and offer it to the incoming queue or drop the
        // packet if it is malformed.
        if (p.verify()) {
            if (!in.offer(p)) {
                TPPNetworkLayer.getLogger().warning(p.toString() + " dropped, NetworkLayer queue full.");
            }
        } else {
            TPPNetworkLayer.getLogger().warning("Link layer delivered malformed packet (" + p.reason() + "), packet dropped.");
            
            if (isPacketFrameLinkLayer()) {
            	this.toPacketFrameLinkLayer().reset();
            }
        }
    }

    public String toString() {
        return "LinkLayerIn" + super.toString();
    }
}
