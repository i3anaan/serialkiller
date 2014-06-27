package link;

import com.google.common.primitives.Bytes;
import network.tpp.Packet;
import network.tpp.PacketHeader;

import java.util.Arrays;

public abstract class PacketFrameLinkLayer extends FrameLinkLayer {
    private byte[] overhead;

    public PacketFrameLinkLayer() {
        overhead = new byte[0];
    }

    public byte[] readPacket() {
        byte[] data = Arrays.copyOf(overhead, overhead.length);

        while (data.length < PacketHeader.HEADER_LENGTH) {
            data = Bytes.concat(data, this.readFrame());
        }

        PacketHeader h = Packet.parseHeader(Arrays.copyOfRange(data, 0, PacketHeader.HEADER_LENGTH));

        int packetLength = h.getLength() + PacketHeader.HEADER_LENGTH;

        while (data.length < packetLength) {
            data = Bytes.concat(data, this.readFrame());
        }

        overhead = Arrays.copyOfRange(data, packetLength, data.length);

        return Arrays.copyOfRange(data, 0, packetLength);
    }
}
