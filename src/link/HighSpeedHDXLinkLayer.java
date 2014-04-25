package link;

import phys.HardwareLayer;
import phys.PhysicalLayer;
import util.Bytes;

public class HighSpeedHDXLinkLayer extends LinkLayer {

	byte expectedAck = Byte.MAX_VALUE;
	byte oldByteSend;
	byte oldByteReceived;
	boolean connectionSetup;
	boolean startWithByte = true;

	public HighSpeedHDXLinkLayer(PhysicalLayer phys) {
		super();
		this.down = phys;
		oldByteSend = 0;
		// Previous input has to be 0;
		oldByteReceived = down.readByte();
		down.sendByte((byte) 0); // Used to make sure both sides start at
									// correct state;
		System.out.println("Send 0 byte");
		connectionSetup = false;
	}

	@Override
	public void sendByte(byte data) {
		byte fullDataToSend = data;
		System.out.println("#######>"+this.hashCode()+"   START SENDING BYTE: "
				+ Bytes.format(data));
		int bitIndex = 0;
		// For every bit in the byte...
		while (bitIndex < 8) {
			// The next bit to send is the LSB.
			byte databit = (byte) (data & 1);
			data >>= 1;
			bitIndex++;
			boolean databitChanged = databit != ((byte) (oldByteSend & 1));
			byte extrabit = 0; // Stores the extra bit, on the correct position
								// (ie,
								// is 0 or 2)

			if (!databitChanged) {
				// Send 1 data bit;
				if ((oldByteSend & 2) == 2) { // Extra bit was 1
					extrabit = 0;
				} else {
					extrabit = 2;
				}
			} else {
				// Send 2 data bits;
				// Thus let extrabit be data;
				extrabit = (byte) (data & 1);
				data >>>= 1; // Wil send 0 if this would be the 9th bit.
				bitIndex++;
				if (bitIndex == 9) {
					System.out.println("Sending 9th bit, filling in with 0");
				}
			}

			// Pack it with the clock and send it.
			byte output = (byte) (0 | extrabit | databit);
			down.sendByte(output);
			oldByteSend = output;
			byte currentIn = down.readByte();
			// Wait for the acknowledgement of the previous bit...
			while (currentIn == oldByteSend) {
				// System.out.println("Awaiting ack, receiving: "+Bytes.format(down.readByte())+"  Expecting: "+Bytes.format(oldBitSend));
				// Waiting till expectedAck is received;
				currentIn = down.readByte();
			}
			oldByteReceived = oldByteSend;
		}

		System.out.println("#######>"+this.hashCode()+"   DONE SENDING BYTE: "
				+ Bytes.format(fullDataToSend));
		connectionSetup = true;
	}

	@Override
	public byte readByte() {
		System.out.println("#######>"+this.hashCode()+"   START READING BYTE, oldReceivedByte = "
				+ Bytes.format(oldByteReceived));
		byte data = 0;

		int bitIndex = 0;
		// For every bit in the byte...
		while (bitIndex < 8) {

			// Read and unpack a line byte
			byte input = down.readByte();
			// System.out.println("Real Received:" + Bytes.format(input));

			// If the clock has changed...
			if (input != oldByteReceived || !connectionSetup) {
				if (!connectionSetup) {
					input = 0;
					connectionSetup = true;
					// Treat first read byte always as 0, even if it isnt
					// actually 0;

				} else {
					connectionSetup = true;

					byte extrabit = (byte) (input & 2);
					byte databit = (byte) (input & 1);
					data |= databit << bitIndex;
					bitIndex++;
					// New data received;
					// Insert the data at the correct location and flip the
					// clock

					boolean databitChanged = databit != ((byte) (oldByteReceived & 1));
					if (databitChanged) {
						// Read second bit
						data |= extrabit << bitIndex;
						bitIndex++;
					}

					System.out.println("Current data received: ["+bitIndex+"]\t"
							+ Bytes.format(data, bitIndex));
					down.sendByte(input);
					oldByteSend = input;
				}
				oldByteReceived = input;
			}
		}

		System.out
				.println("#######>"+this.hashCode()+"   DONE READING BYTE " + Bytes.format(data));
		return data;
	}

	/**
	 * Bits send: 9 Going to wait for ack Filtered Received:00000001 databit: 1
	 * extrabit: 0 first databit, Current data received: 100000 Second databit,
	 * Current data received: 0100000 Bits received: 7
	 */
}
