package link;

import lpt.Lpt;

/**
 * Handles communication over the physical link. Splits and merges whole bytes to/from a format that is accepted by the
 * link.
 */
public class SingleDirectionLinkLayer implements LinkLayer {
    /** The driver class that is used. */
	private Lpt lpt;

    /** The previously received byte. */
	private byte oldByte;

    /** Whether debug mode is enabled. May cause prints and/or waits. */
    private boolean debug;

    /**
     * Constructs a new SingleDirectionLinkLayer instance.
     * @param lpt The driver class to use.
     */
    public SingleDirectionLinkLayer(Lpt lpt) {
        this.lpt = lpt;
    }

    /**
     * Constructs a new SingleDirectionLinkLayer instance.
     * @param lpt The driver class to use.
     * @param debug Whether to enable debug mode. May cause prints and/or waits.
     */
	public SingleDirectionLinkLayer(Lpt lpt, boolean debug) {
        this.lpt = lpt;
        this.debug = debug;

        // Set the old byte to the current state of the link
        this.oldByte = lpt.readLPT();
	}

    @Override
	public void sendByte(byte data) {
        byte oldBit = Byte.MAX_VALUE;

        // Loop over the bits in the byte
		for (int i = 0; i <8; i++) {
			byte bit = (byte)(((data>>i) & 1)); // The bit to send (results in all zero's except the LSB)
			byte aBit = (byte)(i%2); // The bit that alternates between 0 and 1
            byte bits = (byte)(bit | (aBit<<1)); // Combined into two bits (LSB contains data)

            // DEBUG: Print current value while waiting for the acknowledgement
            if(debug) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    System.err.println("Error while waiting to send #" + i);
                }
            }

            // Format current return value
            byte ack = (byte)((lpt.readLPT()<<2)>>7);

            // Wait until the previous transmission is acknowledged
            while(oldBit != Byte.MAX_VALUE && ack != oldBit) {
                // DEBUG: Wait and print current value while waiting for acknowledgement
                if(debug) {
                    System.out.println("  Waiting for " + oldBit + ", currently on " + ack);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        System.err.println("Error while trying to sleep.");
                    }
                }

                // Format current return value again
                ack = (byte)((lpt.readLPT()<<2)>>7);
            }

            // DEBUG: Print that the transmission is acknowledged.
            if(debug) {
                System.out.println("  ACKED");
            }

            // Write the next bit
            lpt.writeLPT(bits);
            oldBit = bit;

            // DEBUG: Print the result of this bit transmission
            if(debug) {
                System.out.println("Sent #" + i + ": " + bits + "  Bit: " + bit + "  ABit: " + aBit);
            }
		}
	}

    @Override
	public byte readByte(){
		byte result = 0; // Resulting byte
		int b = 0; // Bit number

        // Loop over the bits in the byte
        while(b < 8){
            byte in = lpt.readLPT();

            // Check for a new value
            if(in != oldByte){
                byte bit = (byte)((in<<2)>>7); // Remove everything but the LSB
                result = (byte)(result | (bit<<b)); // Add the bit to its relevant position in the result

                // Administrative tasks
                oldByte = in;
                b++;

                // DEBUG: Print data relevant to the received bit
                if(debug) {
                    System.out.println("Received #" + b + ": " + in + "  Bit: " + bit + "  SubResult: " + result);
                }

                // Return received value as acknowledgement
                lpt.writeLPT(bit);

                // DEBUG: Print that the acknowledgement is sent
                if(debug) {
                    System.out.println("  ACK " + bit);
                }
            }
        }

		return result;
	}
}
