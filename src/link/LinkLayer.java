package link;

import lpt.Lpt;

/**
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
        // Loop over the bits in the byte
		for (int i = 0; i <8; i++) {
			byte bit = (byte)(((data>>i) & 1)); // The bit to send (results in all zero's except the LSB)
			byte aBit = (byte)(i%2); // The bit that alternates between 0 and 1
            byte bits = (byte)(bit | (aBit<<1));
			lpt.writeLPT(bits);
            System.out.println("Sent: " + bits + "  Bit: " + bit + "  ABit: " + aBit);
		}
	}

    /**
     * Reads a byte from the link.
     * @return The received byte
     */
	public byte readByte(){
		byte result = 0; // Resulting byte
		int b = 0; // Bit number

        // Loop over the bits in the byte
		while(b<8){
			byte in = lpt.readLPT();

            // Check for a new value
			if(in!=oldByte){
                byte bit = (byte)((in<<7)>>7); // Remove everything but the LSB
                result = (byte)(result & (bit<<b)); // Add the bit to its relevant position in the result

                // Administrative tasks
				oldByte = in;
				b++;
                System.out.println("Received: " + in + "  Bit: " + bit);
			}
		}
		return result;
	}
}
