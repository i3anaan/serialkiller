package network.handlers;

import application.ApplicationLayer;
import network.NetworkLayer;
import network.Packet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Handler for the application layer.
 */
public class ApplicationLayerHandler extends Handler {
    private String name = "ApplicationHandler";
    public static final int SAFE_SEGNUM = (int) Math.min(Integer.MAX_VALUE, Packet.MAX_SEGNUM);

    /** The application layer to talk to. */
    ApplicationLayer app;

    /** Holds segments that needs to be reassembled. */
    Map<Integer, TreeMap<Integer, Packet>> segments;

    /** Keeps track of known sequence sizes. */
    Map<Integer, Integer> sequenceSizes;


    /**
     * Constructs a new ApplicationLayerHandler that talks to the given
     * ApplicationLayer instance.
     * @param app The ApplicationLayer instance.
     */
    public ApplicationLayerHandler(NetworkLayer parent, ApplicationLayer app) {
        super(parent);
        segments = new HashMap<Integer, TreeMap<Integer, Packet>>();
        sequenceSizes = new HashMap<Integer, Integer>();
        this.app = app;
    }

    @Override
    public void handle() {
        try {
            Packet p = out.take();
            int seqnum = p.header().getSeqnum();
            int segnum = p.header().getSegnum();
            boolean more = p.header().getMore();

            // Check if the payload is segmented.
            if (more || segnum != 0) {
                // Check for overflow / DoS.
                if (segnum > SAFE_SEGNUM) {
                    // Drop whole sequence.
                    segments.remove(seqnum);
                    return;
                }
                // Add new sequence to segments map.
                if (!segments.containsKey(seqnum)) {
                    segments.put(seqnum, new TreeMap<Integer, Packet>());
                }
                if (!sequenceSizes.containsKey(seqnum)) {
                    sequenceSizes.put(seqnum, 0);
                }

                // Add this segment to segments map.
                segments.get(seqnum).put(segnum, p);

                // If this packet is the last segment, set total.
                if (!more) {
                    sequenceSizes.put(seqnum, segnum);
                }

                // Check if we have all segments and concatenate data.
                if (sequenceSizes.get(seqnum) == segments.get(seqnum).size()) {
                    // Send concatenated payload to application.
                    app.readPayload(Packet.concatPayloads(segments.get(seqnum).values()));

                    // Cleanup.
                    segments.remove(seqnum);
                    sequenceSizes.remove(seqnum);
                }

                // Done.
                return;
            } else {
                // Simple payload.
                app.readPayload(p.payload());
                return;
            }
        } catch (InterruptedException e) {
            // TODO: Log
            this.stop();
        }
    }
}
