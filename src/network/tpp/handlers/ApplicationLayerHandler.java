package network.tpp.handlers;

import network.tpp.TPPNetworkLayer;
import network.tpp.Packet;
import network.Payload;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Handler for the application layer.
 */
public class ApplicationLayerHandler extends Handler {
    public static final int SAFE_SEGNUM = (int) Math.min(Integer.MAX_VALUE, Packet.MAX_SEGNUM);

    /** The queue to the application layer. */
    ArrayBlockingQueue<Payload> appQueue;

    /** Holds segments that needs to be reassembled. */
    Map<Byte, HashMap<Integer, TreeMap<Integer, Packet>>> segments;

    /** Keeps track of known sequence sizes. */
    Map<Byte, HashMap<Integer, Integer>> sequenceSizes;


    /**
     * Constructs a new ApplicationLayerHandler that talks to the given
     * ApplicationLayer instance.
     * @param appQueue The queue for the application layer.
     */
    public ApplicationLayerHandler(TPPNetworkLayer parent, ArrayBlockingQueue<Payload> appQueue) {
        super(parent);
        segments = new TreeMap<Byte, HashMap<Integer, TreeMap<Integer, Packet>>>();
        sequenceSizes = new TreeMap<Byte, HashMap<Integer, Integer>>();
        this.appQueue = appQueue;
    }

    @Override
    public void handle() throws InterruptedException {
        Packet p = out.take();
        int seqnum = p.header().getSeqnum();
        int segnum = p.header().getSegnum();
        byte sender = p.header().getSender();
        boolean more = p.header().getMore();

        // Check if the payload is segmented.
        if (more || segnum != 0) {
            // Check for overflow / DoS.
            if (segnum > SAFE_SEGNUM) {
                // Drop whole sequence.
                segments.get(sender).remove(seqnum);
                TPPNetworkLayer.getLogger().warning(p.toString() + " exceeds the safe segment number size. All segments dropped.");
                return;
            }
            // Check for host in maps.
            if (!segments.containsKey(sender)) {
                segments.put(sender, new HashMap<Integer, TreeMap<Integer, Packet>>());
            }
            if (!sequenceSizes.containsKey(sender)) {
                sequenceSizes.put(sender, new HashMap<Integer, Integer>());
            }
            // Add new sequence to segments map.
            if (!segments.get(sender).containsKey(seqnum)) {
                segments.get(sender).put(seqnum, new TreeMap<Integer, Packet>());
            }
            if (!sequenceSizes.get(sender).containsKey(seqnum)) {
                sequenceSizes.get(sender).put(seqnum, 0);
            }

            // Add this segment to segments map.
            segments.get(sender).get(seqnum).put(segnum, p);

            // If this packet is the last segment, set total.
            if (!more) {
                sequenceSizes.get(sender).put(seqnum, segnum);
            }

            // Check if we have all segments and concatenate data.
            if (sequenceSizes.get(sender).get(seqnum) == segments.get(sender).get(seqnum).size()) {
                // Send concatenated payload to application.
                appQueue.put(new Payload(Packet.concatPayloads(segments.get(sender).get(seqnum).values()), sender));

                // Cleanup.
                segments.get(sender).remove(seqnum);
                sequenceSizes.get(sender).remove(seqnum);
            }
        } else {
            // Simple payload.
            appQueue.put(new Payload(p.payload(), sender));
        }
    }

    public String toString() {
        return "ApplicationLayer" +  super.toString();
    }
}
