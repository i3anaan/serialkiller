package network;

import link.LinkLayer;

import java.util.Arrays;

/**
 * Handler for data from the link layer.
 */
public class LinkLayerInHandler extends LinkLayerHandler {
    public LinkLayerInHandler(NetworkLayer parent, LinkLayer link) {
        super(parent, link);
    }

    @Override
    public void handle() {
        // Get new data frame from the link.
        byte[] data = link.readFrame(); // TODO: This method is not in the API yet.

        // Make sure the data fits in a packet, otherwise crop it.
        if (data.length > Packet.MAX_PACKET_LENGTH) {
            data = Arrays.copyOfRange(data, 0, Packet.MAX_PACKET_LENGTH);
        }

        // Reconstruct the packet.
        Packet p = new Packet(data);

        // Verify the integrity and offer it to the incoming queue or drop the
        // packet if it is malformed.
        if (p.verify()) {
            in.offer(p);
        }
    }
}
