package link;

import phys.HardwareLayer;
import phys.PhysicalLayer;

public class HighSpeedHDXLinkLayer extends LinkLayer {

	byte expectedAck = Byte.MAX_VALUE;
	byte oldBitSend;
	byte oldBitReceived;
	boolean connectionSetup;

	public HighSpeedHDXLinkLayer(PhysicalLayer phys) {
		super();
		this.down = phys;
		oldBitSend = 0;
		// Previous input has to be 0;
		oldBitReceived = down.readByte();
		down.sendByte((byte)0); //Used to make sure both sides start at correct state;
		connectionSetup = false;
	}

	@Override
	public void sendByte(byte data) {
		// TODO sync before the very first byte sent (put both ends on 0)

		int bitIndex = 0;
		// For every bit in the byte...
		while (bitIndex < 8) {
			// The next bit to send is the LSB.
			byte databit = (byte) (data & 1);
			data >>= 1;
			bitIndex++;
			boolean databitChanged = databit != ((byte) (oldBitSend & 1));
			byte extrabit; // Stores the extra bit, on the correct position (ie,
							// is 0 or 2)

			if (!databitChanged) {
				// Send 1 data bit;
				if ((oldBitSend & 2) == 2) { // Extra bit was 1
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

			// Wait for the acknowledgement of the previous bit...
			while (expectedAck != Byte.MAX_VALUE
					&& down.readByte() != expectedAck) {
				// Waiting till expectedAck is received;
			}

			// Pack it with the clock and send it.
			byte output = (byte) (0 | extrabit | databit);
			down.sendByte(output);

			expectedAck = HardwareLayer.shuftLeft(output);

			data >>= 1; // Shift the sent bit off.
		}
	}

	@Override
	public byte readByte() {
		byte data = 0;

		int bitIndex = 0;
		// For every bit in the byte...
		while (bitIndex < 8) {

			// Read and unpack a line byte
			byte input = down.readByte();
			

			// If the clock has changed...
			if (input != oldBitReceived) {
				if(!connectionSetup){
					input = 0;
					//Treat first read byte always as 0, even if it isnt actually 0;
					connectionSetup = true;
				}
				byte databit = (byte) (input & 1);
				data |= databit << bitIndex;
				bitIndex++;
				// New data received;
				// Insert the data at the correct location and flip the clock
				
				boolean databitChanged = databit != ((byte) (oldBitReceived & 1));
				if(databitChanged){
					//Read second bit
					databit = (byte) (input & 2);
					data |= databit << bitIndex;
					bitIndex++;
				}

				oldBitReceived = input;

				// Send acknowledgement
				down.sendByte(HardwareLayer.shuftLeft(input));
			}
		}

		// System.out.println("returning data " + Bytes.format(data));
		return data;
	}

}
