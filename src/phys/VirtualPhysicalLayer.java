package phys;

/**
 * An implementation of a physical layer that can be used to connect different
 * threads on the same machine.
 */
public class VirtualPhysicalLayer extends PhysicalLayer {
	private VirtualPhysicalLayer that;
	private byte state;

	/** Connect this instance to another instance of VirtualPhysicalLayer. */
	public void connect(VirtualPhysicalLayer that) {
		this.that = that;
		this.state = 0;
	}

	@Override
	public void sendByte(byte data) {
		synchronized (this) {
			synchronized (that) {
				that.takeByte(data);
			}
		}
	}

	@Override
	public byte readByte() {
		synchronized (this) {
			return state;
		}
	}

	/** Takes a byte sent by (possibly) another thread. */
	public void takeByte(byte data) {
		state = data;
	}
}
