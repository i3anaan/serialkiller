package phys;

/**
 * The CheckingPhysicalLayer does some assertions on both data sent and
 * received, checking that they match the expected format. It is not a proper
 * physical layer by itself, but designed to be used on top of a real physical
 * layer (hardware or otherwise).
 * 
 */
public class CheckingPhysicalLayer extends PhysicalLayer {
	protected PhysicalLayer down;
	
	/** Construct a new CheckingPhysicalLayer. */
	public CheckingPhysicalLayer(PhysicalLayer down) {
		super();
		this.down = down;
	}

	@Override
	public void sendByte(byte data) {
		if ((data & 3) != data) {
			String bits = String.format("%08d", Integer.parseInt(Integer.toBinaryString(data)));
			throw new RuntimeException("Sent byte " + bits + " does not match expected 000000XX");
		}
		
		down.sendByte(data);
	}

	@Override
	public byte readByte() {
		byte out = down.readByte();
		if ((out & 3) != out) {
			String bits = String.format("%08d", Integer.parseInt(Integer.toBinaryString(out)));
			throw new RuntimeException("Received byte " + bits + " does not match expected 000000XX");
		}
		return out;
	}

}
