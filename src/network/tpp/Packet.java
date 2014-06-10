package network.tpp;

import com.google.common.primitives.Bytes;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * Transport/network layer packet.
 *
 * Implements the Delayed interface so it can be used in DelayQueues.
 *
 * Protocol: https://github.com/cschutijser/tpp/blob/master/transport-network.md
 */
public class Packet implements Delayed {
    public static final int HEADER_LENGTH = PacketHeader.HEADER_LENGTH;
    public static final int MAX_TTL = PacketHeader.MAX_TTL;
    public static final int MAX_SEQNUM = PacketHeader.MAX_SEQNUM;
    public static final int MAX_SEGNUM = PacketHeader.MAX_SEGNUM;
    public static final int MAX_PAYLOAD_LENGTH = 1024;
    public static final int MAX_PACKET_LENGTH = HEADER_LENGTH + MAX_PAYLOAD_LENGTH;

    /** The header. */
    private PacketHeader header;

    /** The payload. */
    private byte[] payload;

    /** Whether the packages is precompiled (that is, not changed). */
    private boolean precompiled = false;

    /** Timestamp. */
    private long timestamp;

    private long delay;
    private TimeUnit delayUnit = TimeUnit.MILLISECONDS;

    /** Number of retransmissions. */
    private int retransmissions;

    /** The reason for rejecting the packet. */
    private RejectReason reason = RejectReason.UNKNOWN;

    /**
     * Constructs a new, empty Packet object with a new, empty header.
     */
    public Packet(int seqnum) {
        header = new PacketHeader();
        header.setSeqnum(seqnum);
        header.setTTL(MAX_TTL);
        payload = new byte[0];
        delay = 0;
    }

    /**
     * Constructs a new Packet object based on the given raw input data.
     * @param raw The raw input data.
     */
    public Packet(byte[] raw) {
        if (raw == null || raw.length < HEADER_LENGTH) {
            throw new IllegalArgumentException("The raw data is not of the correct size.");
        }

        header = new PacketHeader(Arrays.copyOfRange(raw, 0, HEADER_LENGTH));
        payload = Arrays.copyOfRange(raw, HEADER_LENGTH, raw.length);
        precompiled = true;
    }

    /**
     * Check whether this package is precompiled and ready to use.
     * @return Whether this package is precompiled.
     */
    public boolean isPrecompiled() {
        return precompiled && header.precompiled;
    }

    /**
     * Get the header of this packet.
     * @return The header of this packet.
     */
    public PacketHeader header() {
        return header;
    }

    /**
     * Get the payload of this packet.
     * @return The payload of this packet.
     */
    public byte[] payload() {
        return payload;
    }

    public void setPayload(byte[] data) {
        if (data.length <= MAX_PAYLOAD_LENGTH) {
            payload = data;
            header.setLength(data.length);
        } else {
            throw new IllegalArgumentException("The payload cannot be longer than 1024 bytes.");
        }
        precompiled = false;
    }

    /**
     * Get the length (in bytes) of the whole packet.
     * @return The packet length in bytes.
     */
    public int length() {
        return header.getLength() + HEADER_LENGTH;
    }

    /**
     * Compile the package and return the byte array containing this package.
     * @return The byte array representing this package
     */
    public byte[] compile() {
        // Checksum
        header.setChecksum(0L);
        Checksum checksum = new CRC32();
        checksum.reset();
        checksum.update(Bytes.concat(header.compile(), payload), 0, length());
        header.setChecksum(checksum.getValue());

        // Return data including the checksum
        return Bytes.concat(header.compile(), payload);
    }

    /**
     * Create a new Packet object that contains an acknowledgement for this
     * Packet.
     * @return The acknowledgement packet.
     */
    public Packet createAcknowledgement(int seqnum) {
        Packet ack = new Packet(seqnum);

        ack.header.setAck(true);
        ack.header.setAcknum(header.getSeqnum());
        ack.header.setSender(header.getDestination());
        ack.header.setDestination(header.getSender());
        ack.header.setSegnum(header.getSegnum());

        return ack;
    }

    /**
     * Verifies the data integrity of this packet.
     * @return Whether this packet is unchanged.
     */
    public boolean verify() {
        boolean valid = true;

        // Verify length
        if (valid) {
            valid = payload().length == header().getLength();

            if (!valid) {
                reason = RejectReason.LENGTH;
            }
        }

        // Verify checksum
        if (valid) {
            // Get original checksum.
            long checksum = this.header().getChecksum();

            // Recalculate checksum.
            this.compile();

            // Check if the checksums are equal.
            valid = this.header().getChecksum() == checksum;

            // Restore original checksum.
            this.header().setChecksum(checksum);

            if (!valid) {
                reason = RejectReason.CHECKSUM;
            }
        }

        return valid;
    }

    /**
     * Clone this packet instance.
     * @return The cloned packet instance.
     */
    public Packet clone() {
        return new Packet(Bytes.concat(header.compile(), payload));
    }

    /**
     * Concatenates the payloads of multiple packets.
     * @param packets The collection of packets.
     * @return The concatenated payloads.
     */
    public static byte[] concatPayloads(Collection<Packet> packets) {
        byte[] result = new byte[0];

        for (Packet packet : packets) {
            result = Bytes.concat(result, packet.payload());
        }

        return result;
    }

    /**
     * [METADATA] Get the meta timestamp.
     * @return The timestamp.
     */
    public long timestamp() {
        return timestamp;
    }

    /**
     * [METADATA] Set the meta timestamp for this packet.
     * @param timestamp The timestamp.
     */
    public void timestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * [METADATA] Returns the amount of retransmissions done for this packet.
     * @return The amount of retransmissions done for this packet.
     */
    public int retransmissions() {
        return retransmissions;
    }

    /**
     * [METADATA] Marks this packet as being retransmitted. Updates a counter.
     */
    public void retransmit() {
        retransmissions++;
    }

    /**
     * [METADATA] Returns the reason for rejecting the packet. This method is
     * only useful right after running the verify() method.
     * @return The reason for rejecting the packet.
     */
    public RejectReason reason() {
        return reason;
    }

    /**
     * [METADATA] Returns the delay used for delay queues.
     * @return The delay.
     */
    public long delay() {
        return this.delay;
    }

    /**
     * [METADATA] Sets the delay used for delay queues.
     * @param delay The delay.
     */
    public void delay(long delay) {
        this.delay = delay;
    }

    /**
     * [METADATA] Returns the delay used for delay queues in the given time
     * unit. This method is used by DelayQueue instances.
     */
    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(delay, delayUnit);
    }

    /**
     * [METADATA] Compares two Packet instances based on the delay. This method
     * is used by DelayQueue instances.
     */
    @Override
    public int compareTo(Delayed o) {
        return (int) (getDelay(delayUnit) - o.getDelay(delayUnit));
    }

    /**
     * [METADATA] Returns the string identifier of this packet.
     * @return The identifier.
     */
    public String id() {
        return Packet.id(header.getDestination(), header.getSeqnum(), header.getSegnum());
    }

    /**
     * Returns a string representation of this object.
     * @return The string representation.
     */
    public String toString() {
        return String.format("Packet<From: %d; To: %d; Seq: %d; Seg: %d; Ack: %d>", header.getSender(), header.getDestination(), header.getSeqnum(), header.getSegnum(), header.getAcknum());
    }

    /**
     * Parses raw data into a new PacketHeader object.
     * @param raw The raw input data.
     * @return The PacketHeader object.
     */
    public static PacketHeader parseHeader(byte[] raw) {
        return new PacketHeader(raw);
    }

    /**
     * Returns a packet string identifier based on the specified parameters.
     * @param host The original destination host.
     * @param seqnum The sequence number.
     * @param segnum The segment number.
     * @return The identifier.
     */
    public static String id(byte host, int seqnum, int segnum) {
        return String.format("%d.%d.%d", host, seqnum, segnum);
    }

    /**
     * Identifies reasons for rejecting a packet after verifying. This is
     * mainly used for logging purposes.
     */
    public enum RejectReason {
        UNKNOWN ("Unknown"),
        LENGTH ("Length"),
        CHECKSUM ("Checksum");

        private String description;

        RejectReason(String description) {
            this.description = description;
        }

        public String toString() {
            return description;
        }
    }
}
