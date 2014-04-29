package link;

import phys.CleanStartPhysicalLayer;
import phys.PhysicalLayer;
import util.Bytes;

public class HighSpeedHDXLinkLayer extends LinkLayer {

	byte oldByteSent; // Should be set after sending something;
	byte oldByteReceived; // Should be set before reading something;
	boolean newInputReceivedSinceSent;

	public HighSpeedHDXLinkLayer(PhysicalLayer down) {
		super();
		this.down = down;
		oldByteSent = 0; // Works when using CleanStart
		oldByteReceived = 0;// Works when using CleanStart

		down.readByte(); //Create block;
	}

	// For full duplex to work, this probably requires a differnet method
	// exchangeByte();
	// This method would exchange one byte on the link.
	// The readFrame() would then simply return the last exchanged frame;
	// The sendFrame() would queue up a frame, or even replace (single frame
	// buffer) the queued frame.
	// It should then be made sure that both readFrame() and sendFrame() are
	// called before calling exchangeFrame() again.
	// ExchangeFrame() should probably not do anything unless both of those
	// methods are called.
	// This to prevent missing reading a frame, or double sending a frame.
	// The exchangeByte() method should contain a timeout, to resolve desyncs.
	// The frames should then be reliable in length, but not in bit errors.
	// (Note though that 1 side may have correct length, while the other doesnt
	// and they cannot know this without explicitly communicating about this.)
	// For this reason it is probably wise to conclude with a final check byte.
	// There should be received enough of the check byte to make sure that it is
	// the check byte.
	// In other words, that start part may have no other outcome (flag or data)
	// Which means this needs to be redundant in length.
	// (Which can be received only half, as the fact that you received half
	// means you have the correct length)

	// BOTTOMLINE: the sendFrame()/readFrame()/exchangeFrame() fully lock one
	// side of the link until it has received the full frame, or a timeout
	// occurs.

	@Override
	public void sendByte(byte data) {
		// down.sendByte(data);
		int bitIndex = 0;
		// For every bit in the byte...
		while (bitIndex < 8) {
			// The next bit to send is the LSB.
			byte databit = (byte) (data & 1);
			data >>= 1;
			bitIndex++;
			System.out.println("OldByteSent: "+Bytes.format(oldByteSent));
			//System.out.println("Databit: "+Bytes.format(databit));
			boolean databitChanged = databit != ((byte) (oldByteSent & 1));
			byte extrabit;

			if (!databitChanged) {
				System.out.println("Databit not changed");
				// Send 1 data bit;
				if ((oldByteSent & 2) == 2) { // Extra bit was 1
					extrabit = 0;
				} else {
					extrabit = 2;
				}
			} else {
				System.out.println("Databit changed");
				// Send 2 data bits;
				// Thus let extrabit be data;
				extrabit = (byte) ((data & 1)*2);
				data >>>= 1; // Will send 0 if this would be the 9th bit.
				bitIndex++; // TODO no longer send 0 to fill 9th bit, but use
							// some form of stream.
				if (bitIndex == 9) {
					System.out.println("Sending 9th bit, filling in with 0");
				}
			}

			
			System.out.println("Databit: "+Bytes.format(databit));
			System.out.println("Extrabit: "+Bytes.format(extrabit));
			// Pack output with databit and extrabit;
			byte output = (byte) (0 | extrabit | databit);
			System.out.println("HighSpeedHDXLinkLayer shipping off byte to lower layer: " + Bytes.format(output));
			down.sendByte(output);
			oldByteSent = output;
			down.readByte(); // Needs to have debouncer for this to work;
		}
	}

	@Override
	public byte readByte() {
		byte data = 0;
		int bitIndex = 0;
		byte input = oldByteReceived;
		// For every bit in the byte...
		while (bitIndex < 8) {
			// Read and unpack a line byte
			// oldByteReceived is set at the end of the previous loop.
			oldByteReceived = input;
			input = down.readByte();

			if (input != oldByteReceived) {
				byte extrabit = (byte) (input & 2);
				byte databit = (byte) (input & 1);
				byte addition;
				int bitsAdded;
				//data |= databit << bitIndex;
				//bitIndex++;
				// New data received;
				// Insert the data at the correct location

				boolean databitChanged = databit != ((byte) (oldByteReceived & 1));
				if (databitChanged) {
					// Read second bit
					addition = (byte) (input & 3);
					bitsAdded = 2;
				}else{
					addition = databit;
					bitsAdded = 1;
				}
				
				data = (byte)(data | addition<<bitIndex);
				bitIndex = bitIndex+bitsAdded;
				System.out.println("Current data received: [" + bitIndex
						+ "]\t" + Bytes.format(data, bitIndex));
				down.sendByte(input);
				oldByteSent = input;
			}
		}
		oldByteReceived = input;
		return data;
	}

}
