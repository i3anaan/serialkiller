package network.handlers;

import link.FrameLinkLayer;
import network.NetworkLayer;
import network.Packet;

import java.util.Arrays;

/**
 * Handler for data from the link layer.
 */
public class LinkLayerInHandler extends LinkLayerHandler {
    private String name = "LinkHandler<In>";

    public LinkLayerInHandler(NetworkLayer parent, FrameLinkLayer link) {
        super(parent, link);
    }

    @Override
    public void handle() {
        // Get new data frame from the link.
        byte[] data = link.readFrame();

        // Make sure the data fits in a packet, otherwise crop it.
        if (data.length > Packet.MAX_PACKET_LENGTH) {
            data = Arrays.copyOfRange(data, 0, Packet.MAX_PACKET_LENGTH);
            NetworkLayer.getLogger().warning("Link layer delivered a packet that is too long, packet cropped.");
        }

        // Reconstruct the packet.
        Packet p = new Packet(data);

        // Verify the integrity and offer it to the incoming queue or drop the
        // packet if it is malformed.
        if (p.verify()) {
            in.offer(p);
        } else {
            NetworkLayer.getLogger().warning("Link layer delivered malformed packet, packet dropped.");
        }
    }
}
