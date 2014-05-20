package network;

import util.BitSet2;
import util.ByteArrays;
import util.Bytes;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

/**
 * Represents a header of a packet.
 */
public class PacketHeader {
    public static final int HEADER_LENGTH = 13;
    public static final int MAX_TTL = 7;
    public static final int MAX_SEQNUM = 255;
    public static final int MAX_SEGNUM = 16777215;

    /** The raw data of this header. */
    private BitSet2 raw;

    /** Whether the header is precompiled (that is, not changed). */
    protected boolean precompiled;

    /**
     * Constructs a new, empty PacketHeader object.
     */
    protected PacketHeader() {
        precompiled = false;
        raw = new BitSet2(HEADER_LENGTH * 8);
    }

    /**
     * Constructs a new PacketHeader object based on the raw data.
     * @param raw The raw header data.
     */
    protected PacketHeader(byte[] raw) {
        if (raw == null || raw.length != HEADER_LENGTH) {
            throw new IllegalArgumentException("The raw data is not of the correct size.");
        }

        this.raw = BitSet2.valueOf(raw);
        precompiled = true;
    }

    protected byte[] compile() {
        return raw.toByteArray();
    }

    protected BitSet raw() {
        return raw;
    }

    public long getChecksum() {
        return ByteArrays.parseLong(ByteArrays.fromBitSet(raw.get(0, 32), 4));
    }

    protected void setChecksum(long checksum) {
        if (checksum >= 0 && checksum < 4294967296L) {
            // Reset checksum
            raw.set(0, 32, false);

            byte[] arr = ByteBuffer.allocate(8).putLong(checksum).array();
            raw.or(ByteArrays.toBitSet(Arrays.copyOfRange(arr, 4, 8), HEADER_LENGTH * 8, 0));
        } else {
            throw new IllegalArgumentException("The checksum is too large.");
        }
        precompiled = true;
    }

    public int getTTL() {
        return (Bytes.fromBitSet(raw, 32) >> 5) & 7;
    }

    protected void setTTL(int TTL) {
        if (TTL >= 0 && TTL <= MAX_TTL) {
            raw.or(Bytes.toBitSet2((byte) (TTL << 5), HEADER_LENGTH * 8, 32));
        } else {
            throw new IllegalArgumentException("The TTL should be between 0 and 7 (inclusive).");
        }
        precompiled = false;
    }

    /**
     * Decreases the TTL with 1, unless the TTL is zero, then it stays zero.
     */
    public void decreaseTTL() {
        if (getTTL() > 0) {
            setTTL(getTTL() - 1);
        }
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
        return ByteArrays.parseLong(new byte[]{(byte) (Bytes.fromBitSet(raw, 32) & 7), Bytes.fromBitSet(raw, 40)});
    }

    protected void setLength(long length) {
        if (length >= 0 && length <= 1024) {
            raw.or(ByteArrays.toBitSet(Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(length).array(), 6, 8), HEADER_LENGTH * 8, 32));
        } else {
            throw new IllegalArgumentException("The length should be between 0 and 1024 (inclusive).");
        }
        precompiled = false;
    }

    public byte getSender() {
        return Bytes.fromBitSet(raw, 48);
    }

    protected void setSender(byte sender) {
        raw.or(Bytes.toBitSet2(sender, HEADER_LENGTH * 8, 48));
        precompiled = false;
    }

    public byte getDestination() {
        return Bytes.fromBitSet(raw, 56);
    }

    protected void setDestination(byte destination) {
        raw.or(Bytes.toBitSet2(destination, HEADER_LENGTH * 8, 56));
        precompiled = false;
    }

    public int getSeqnum() {
        return (int) Bytes.fromBitSet(raw, 64);
    }

    protected void setSeqnum(int seqnum) {
        if (seqnum >= 0 && seqnum <= MAX_SEQNUM) {
            raw.or(Bytes.toBitSet2((byte) seqnum, HEADER_LENGTH * 8, 64));
        } else {
            throw new IllegalArgumentException("The sequence number should be between 0 and 255 (inclusive).");
        }
        precompiled = false;
    }

    public int getAcknum() {
        return (int) Bytes.fromBitSet(raw, 72);
    }

    protected void setAcknum(long acknum) {
        if (acknum >= 0 && acknum <= MAX_SEQNUM) {
            raw.or(Bytes.toBitSet2((byte) acknum, HEADER_LENGTH * 8, 72));
        } else {
            throw new IllegalArgumentException("The acknowledgement number should be between 0 and 255 (inclusive).");
        }
        precompiled = false;
    }

    public int getSegnum() {
        return (int) ByteArrays.parseLong(ByteArrays.fromBitSet(raw.get(80, 104)));
    }

    protected void setSegnum(int segnum) {
        if (segnum >= 0 && segnum <= MAX_SEGNUM) {
            raw.or(ByteArrays.toBitSet(Arrays.copyOfRange(ByteBuffer.allocate(8).putLong(segnum).array(), 5, 8), HEADER_LENGTH * 8, 80));
        } else {
            throw new IllegalArgumentException("The segment number should be between 0 and 16777215 (inclusive).");
        }
        precompiled = false;
    }

}
