package link;

import phys.PhysicalLayer;

/**
 * A half-duplex link-layer implementation that uses transitions between states
 * to convey data bits and acknowledgments.
 * 
 * The layer is half-duplex because any data sent by the other side while we're
 * sending is silently ignored.
 * 
 * This link layer awaits acknowledgment for every bit sent.
 */
public class StatefulHDXLinkLayer extends LinkLayer {
	private int ourState = 0;
	private int theirState = 0;
	
	public static final int NO_CHANGE = 0;
	public static final int ZERO_BIT_ACK = 1;
	public static final int ONE_BIT_ACK = 2;
	public static final int NO_BIT_ACK = 3;
	
	public static final int encode(int state, int transition) {
		return (state + transition + 4) % 4;
	}
	
	public static final int decode(int old_state, int new_state) {
		return (new_state - old_state + 4) % 4;
	}
	
	public StatefulHDXLinkLayer(PhysicalLayer down) {
		super();
		this.down = down;
	}

	@Override
	public void sendByte(byte data) {
		// For every bit in the byte...
		for (int i = 0; i < 8; i++) {
			// Pop off the LSB
			int databit = data & 1;
			
			// Modify our state to match
			ourState = encode(ourState, databit + 1);
			down.sendByte((byte) ourState);
			
			// Wait for the other side to change their state (any change is ack)
			while (true) {
				int theirNewState = down.readByte();
				if (theirNewState != theirState) {
					theirState = theirNewState;
					break;
				}
			}
			
			// We're done!
			data >>= 1;
		}
	}
	
	@Override
	public byte readByte() {
		int out = 0;
		
		// For every bit in the byte...
		for (int i = 0; i < 8;) {
			// Read something from the other side
			byte in = down.readByte();
			int diff = decode(theirState, in);
			
			switch (diff) {
			case NO_CHANGE:
				continue;
			case NO_BIT_ACK: continue;
			case ONE_BIT_ACK:
				out <<= 1;
				out |= 1;
				i++;
				break;
			case ZERO_BIT_ACK:
				out <<= 1;
				out |= 0; // for symmetry
				i++;
				break;
			}
			
			ourState = encode(ourState, NO_BIT_ACK);
			down.sendByte((byte) ourState);
			theirState = in;
		}
		
		return (byte) out;
	}

}
