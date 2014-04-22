package link;

import lpt.Lpt;

/**
 * Alternate implementation of the LinkLayer that uses framing.
 * Handles communication over the physical link. Splits and merges whole bytes to/from a format that is accepted by the
 * link.
 */
public class LinkLayer {
	/** The driver class that is used. */
	private Lpt lpt;

	/** The previously received byte. */
	private byte oldByte = Byte.MAX_VALUE;

	public LinkLayer(Lpt lpt) {
		this.lpt = lpt;
	}

	/**
	 * Sends the given byte over the link.
	 * @param data The data to send.
	 */
	public void sendByte(byte data) {
		byte oldBit = Byte.MAX_VALUE;

		// Loop over bytes in the frame, final byte is flag byte
		for(int j = 0; j<7; j++){


			// Loop over the bits in the byte
			for (int i = 0; i <8; i++) {
				byte bit = (byte)(((data>>i) & 1)); // The bit to send (results in all zero's except the LSB)
				//byte aBit = (byte)(i%2); // The bit that alternates between 0 and 1
				//byte bits = (byte)(bit | (aBit<<1));
				byte bits = (byte)bit;

				try {
					Thread.sleep(20);
				} catch (InterruptedException e) {
					System.err.println("Error while waiting to send #" + i);
				}
				// replace with  check for frames instead of bits
				while(oldBit != Byte.MAX_VALUE && lpt.readLPT() != oldBit) {
					// Wait until the previous transmission is acknowledged.
					System.out.println("  Waiting for " + oldBit + ", currently on " + lpt.readLPT());
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				//end block
				System.out.println("  ACKED");
				lpt.writeLPT(bits);
				oldBit = bit;
				System.out.println("Sent #" + i + ": " + bits + "  Bit: " + bit);
				
			}
		}
	}

	/**
	 * Reads a byte from the link.
	 * @return The received byte
	 */
	public byte readByte(){
		byte result = 0; // Resulting byte
		int b = 0; // Bit number

		// Do not use the phantom reading from the previous transmission
		if (oldByte == Byte.MAX_VALUE) {
			oldByte = lpt.readLPT();
		}

		// Loop over the bits in the byte
		while(b<8){
			byte in = lpt.readLPT();

			// Check for a new value
			if(in!=oldByte){
				byte bit = (byte)((in<<2)>>7); // Remove everything but the LSB
				result = (byte)(result | (bit<<b)); // Add the bit to its relevant position in the result

				// Administrative tasks
				oldByte = in;
				b++;
				System.out.println("Received #" + b + ": " + in + "  Bit: " + bit + "  SubResult: " + result);

				// Echo received value
				lpt.writeLPT(bit);
				System.out.println("  ACK " + bit);
			}
		}

		return result;
	}
	
	public void readFrame(){
		//Todo : build method for interpeting bytes and then frames in that order
	}
	
	public void sendFlag(){
		//Todo: build method for sending flag byte
	}
	
	public void readFlag(){
		//Todo: build method for reading flag byte as 8th byte in series
	}
}
