package phys.diag;

import phys.PhysicalLayer;

/**
 * A physical layer/filter that debounces its input. Any repeated bytes returned
 * by the lower layer's readByte are ignored. This should work quite well with
 * link layers that use something of a clock byte.
 * 
 * Bytes being sent through this layer are passed through unmodified.
 */
public class DebouncePhysicalLayer extends PhysicalLayer {
	protected PhysicalLayer down;
	private Byte lastRead = null;

	public DebouncePhysicalLayer(PhysicalLayer down) {
		super();
		this.down = down;
	}

	@Override
	public void sendByte(byte data) {
		down.sendByte(data);
	}

	@Override
	public byte readByte() {
		while (true) {
			Byte in = down.readByte();

			if (lastRead == null || !lastRead.equals(in)) {
				// Got a fresh byte, our work here is done
				return lastRead = in;
			}
		}
	}

}
