package network;

import util.ByteArrays;
import util.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Represents a header of a packet.
 */
public class PacketHeader {
    public static int HEADER_LENGTH = 13;

    /** The raw data of this header. */
    private BitSet raw;

    /** Whether the header is precompiled (that is, not changed). */
    protected boolean precompiled;

    /**
     * Constructs a new, empty PacketHeader object.
     */
    protected PacketHeader() {
        precompiled = false;
        raw = new BitSet(HEADER_LENGTH * 8);
    }

    /**
     * Constructs a new PacketHeader object based on the raw data.
     * @param raw The raw header data.
     */
    protected PacketHeader(byte[] raw) {
        if (raw == null || raw.length != HEADER_LENGTH) {
            throw new IllegalArgumentException("The raw data is not of the correct size.");
        }

        this.raw = ByteArrays.toBitSet(raw);
        precompiled = true;
    }

    protected byte[] compile() {
        return ByteArrays.fromBitSet(raw);
    }

    public long getChecksum() {
        return ByteArrays.parseLong(ByteArrays.fromBitSet(raw.get(0, 32)));
    }

    protected void setChecksum(long checksum) {
        if (checksum >= 0 && checksum < 4294967296L) {
            raw.or(ByteArrays.toBitSet(Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(checksum).array(), 4, 8), HEADER_LENGTH * 8, 0));
        } else {
            throw new IllegalArgumentException("The checksum is too large.");
        }
        precompiled = true;
    }

    public int getTTL() {
        return (Bytes.fromBitSet(raw, 32) >> 5);
    }

    protected void setTTL(int TTL) {
        if (TTL >= 0 && TTL < 7) {
            raw.or(Bytes.toBitSet((byte) TTL, HEADER_LENGTH * 8, 32));
        } else {
            throw new IllegalArgumentException("The TTL should be between 0 and 7 (inclusive).");
        }
        precompiled = false;
    }

    public boolean getAck() {
        return raw.get(35);
    }

    protected void setAck(boolean ack) {
        raw.set(35, ack);
        precompiled = false;
    }

    public boolean getMore() {
        return raw.get(36);
    }

    protected void setMore(boolean more) {
        raw.set(36, more);
        precompiled = false;
    }

    public long getLength() {
        return ByteArrays.parseLong(ByteArrays.fromBitSet(raw.get(37, 48)));
    }

    protected void setLength(long length) {
        if (length >= 0 && length <= 1024) {
            raw.or(ByteArrays.toBitSet(Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(length).array(), 2, 4), HEADER_LENGTH * 8, 32));
        } else {
            throw new IllegalArgumentException("The length should be between 0 and 1024 (inclusive).");
        }
        precompiled = false;
    }

    public byte getSender() {
        return Bytes.fromBitSet(raw, 48);
    }

    protected void setSender(byte sender) {
        raw.or(Bytes.toBitSet(sender, HEADER_LENGTH * 8, 48));
        precompiled = false;
    }

    public byte getDestination() {
        return Bytes.fromBitSet(raw, 64);
    }

    protected void setDestination(byte destination) {
        raw.or(Bytes.toBitSet(destination, HEADER_LENGTH * 8, 56));
        precompiled = false;
    }

    public long getSeqnum() {
        return ByteArrays.parseLong(ByteArrays.fromBitSet(raw.get(64, 72)));
    }

    protected void setSeqnum(long seqnum) {
        if (seqnum >= 0 && seqnum < 256) {
            raw.or(Bytes.toBitSet(ByteBuffer.allocate(8).putLong(seqnum).array()[3], HEADER_LENGTH * 8, 64));
        } else {
            throw new IllegalArgumentException("The sequence number should be between 0 and 255 (inclusive).");
        }
        precompiled = false;
    }

    public long getAcknum() {
        return ByteArrays.parseLong(ByteArrays.fromBitSet(raw.get(72, 80)));
    }

    protected void setAcknum(long acknum) {
        if (acknum > 0 && acknum < 256) {
            raw.or(Bytes.toBitSet(ByteBuffer.allocate(8).putLong(acknum).array()[3], HEADER_LENGTH * 8, 72));
        } else {
            throw new IllegalArgumentException("The acknowledgement number should be between 0 and 255 (inclusive).");
        }
        precompiled = false;
    }

    public long getSegnum() {
        return ByteArrays.parseLong(ByteArrays.fromBitSet(raw.get(80, 104)));
    }

    protected void setSegnum(long segnum) {
        if (segnum > 0 && segnum < 256) {
            raw.or(Bytes.toBitSet(ByteBuffer.allocate(8).putLong(segnum).array()[3], HEADER_LENGTH * 8, 80));
        } else {
            throw new IllegalArgumentException("The segment number should be between 0 and 255 (inclusive).");
        }
        precompiled = false;
    }

}
