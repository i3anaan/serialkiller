package network;

import com.google.common.primitives.Bytes;

import java.util.Arrays;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Packet {
    public static int HEADER_LENGTH = PacketHeader.HEADER_LENGTH;
    public static int MAX_PAYLOAD_LENGTH = 1024;

    /** The header. */
    private PacketHeader header;

    /** The payload. */
    private byte[] payload;

    /** Whether the packages is precompiled (that is, not changed). */
    private boolean precompiled = false;

    /**
     * Constructs a new, empty Packet object with a new, empty header.
     */
    public Packet() {
        header = new PacketHeader();
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
     * @return Wheter this package is precompiled.
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
        return (int) header.getLength() + HEADER_LENGTH;
    }

    /**
     * Compile the package and return the byte array containing this package.
     * @return The byte array representing this package
     */
    public byte[] compile() {
        // Checksum
        Checksum checksum = new CRC32();
        checksum.update(Bytes.concat(header.compile(), payload), 0, length());

        // Return data including the checksum
        return Bytes.concat(header.compile(), payload);
    }
}
