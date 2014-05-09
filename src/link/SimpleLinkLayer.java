package link;

import phys.PhysicalLayer;

/**
 * A very simple half-duplex link layer that just splits bytes into single bits,
 * adds an alternating clock bit, and ships it all off to the layer below.
 * 
 * The input byte is split in 8 'bytes' of PHY layer data. The LSB is sent
 * first. In the byte that is shipped to the physical layer, the LSB is the data
 * bit, while the other bit contains the 'clock'.
 * 
 * This link layer will get terribly confused if it misses even a single state,
 * or if even a single bit error occurs. It will either hang or start returning
 * obviously wrong data.
 * 
 * There is no acknowledgment mechanism and senders will not wait for receivers
 * in any way. This means SimpleLinkLayer will only work when used with a
 * physical layer wrapped with DelayPhysicalLayer.
 */
public class SimpleLinkLayer extends BytewiseLinkLayer {
	PhysicalLayer down;
	
	/** The physical layer implementation under this link layer. */
	public SimpleLinkLayer(PhysicalLayer down) {
		super();
		this.down = down;
	}

	@Override
	public void sendByte(byte input) {
		byte clock = 1;

		// For every bit in the byte...
		for (int i = 0; i < 8; i++) {
			// The next bit to send is the LSB.
			byte databit = (byte) (input & 1);

			// Pack it with the clock and send it.
			down.sendByte((byte) (0 | (clock << 1) | databit));

			input >>= 1; // Shift the sent bit off.
			clock ^= 1; // Invert the clock 'bit'
		}
	}

	@Override
	public byte readByte() {
		byte data = 0;
		byte oldclock = 0;

		// For every bit in the output byte...
		for (int i = 0; i < 8; /* */) {
			// Read and unpack a line byte 
			byte input = down.readByte();
			byte clockbit = (byte) ((input >> 1) & 1);
			byte databit = (byte) (input & 1);
			
			// If the clock has changed...
			if (clockbit != oldclock) {
				// Insert the data at the correct location and flip the clock
				data |= databit << i;

				i++; oldclock = clockbit;
			}
		}

		// System.out.println("returning data " + Bytes.format(data));
		return data;
	}

}
