package link;

import phys.PhysicalLayer;

/**
 * A half-duplex link layer implementation that changes the return channel data
 * as an acknowledgement.
 *
 * The input byte is split in 8 'bytes' of PHY layer data. The LSB is sent
 * first. In the byte that is shipped to the physical layer, the LSB is the data
 * bit, while the other bit contains the 'clock'.
 *
 * The sender will wait for an acknowledgement before trying to transmit the
 * next bit, so bits are transferred correctly. However, when bit faults occur,
 * the sendByte method may send data too fast (a change in the return channel
 * can be seen as an ack, even when it is not).
 */
public class AckLinkLayer extends LinkLayer {
    /**
     * Constructs a new AckLinkLayer instance.
     * @param down The driver class to use.
     */
    public AckLinkLayer(PhysicalLayer down) {
        super();
        this.down = down;
    }

    @Override
	public void sendByte(byte input) {
        byte clock = 1;
        byte expectedAck = Byte.MAX_VALUE;

        // For every bit in the byte...
        for (int i = 0; i < 8; i++) {
            // The next bit to send is the LSB.
            byte databit = (byte) (input & 1);

            // Wait for the acknowledgement of the previous bit...
            while (expectedAck != Byte.MAX_VALUE
                    && down.readByte() != expectedAck) {
                // Waiting...
            }

            // Pack it with the clock and send it.
            byte output = (byte) (0 | (clock << 1) | databit);
            down.sendByte(output);

            expectedAck = (byte)(0 | (clock << 3) | (databit << 7));

            input >>= 1; // Shift the sent bit off.
            clock ^= 1; // Invert the clock 'bit'
        }
	}

    @Override
	public byte readByte(){
        byte data = 0;
        byte oldclock = 0;

        // For every bit in the output byte...
        for (int i = 0; i < 8; /* */) {
            // Read and unpack a line byte
            byte input = down.readByte();
            byte clock = (byte) ((input >> 1) & 1);
            byte databit = (byte) (input & 1);

            // If the clock has changed...
            if (clock != oldclock) {
                // Insert the data at the correct location and flip the clock
                data |= databit << i;

                i++; oldclock = clock;

                // Send acknowledgement
                down.sendByte((byte) (0 | (clock << 1) | databit));
            }
        }

        // System.out.println("returning data " + Bytes.format(data));
        return data;
    }
}
