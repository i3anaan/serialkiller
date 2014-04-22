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
        byte oldBits = Byte.MAX_VALUE;

        // Loop over the bits in the byte
		for (int i = 0; i <8; i++) {
			byte bit = (byte)(((data>>i) & 1)); // The bit to send (results in all zero's except the LSB)
			byte aBit = (byte)(i%2); // The bit that alternates between 0 and 1
            byte bits = (byte)(bit | (aBit<<1));

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                System.err.println("Error while waiting to send #" + i);
            }
            while(oldBits != Byte.MAX_VALUE && lpt.readLPT() != oldBits) {
                // Wait until the previous transmission is acknowledged.
                System.out.println("  Waiting for " + oldBits + ", currently on " + lpt.readLPT());
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("  ACKED");
            lpt.writeLPT(bits);
            oldBits = bits;
            System.out.println("Sent #" + i + ": " + bits + "  Bit: " + bit + "  ABit: " + aBit);
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
                lpt.writeLPT(in);
                System.out.println("  ACK " + in);
            }
        }

		return result;
	}
}
