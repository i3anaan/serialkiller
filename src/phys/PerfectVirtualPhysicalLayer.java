package phys;

import java.util.concurrent.ArrayBlockingQueue;

public class PerfectVirtualPhysicalLayer extends PhysicalLayer {
	private ArrayBlockingQueue<Byte> inbox;
	private PerfectVirtualPhysicalLayer that;
	public PerfectVirtualPhysicalLayer() {

	}
	/** Connect this instance to another instance of PerfectVirtualPhysicalLayer. */
	public void connect(PerfectVirtualPhysicalLayer that) {
		this.that = that;
		this.inbox = new ArrayBlockingQueue<Byte>(1024);
	}
	@Override
	public void sendByte(byte data) {
		that.takeByte(data);
	}

	@Override
	public byte readByte() {
		try {
			return inbox.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return 0;
		}
	}
	public void takeByte(byte b) {
		try {
			inbox.put((byte) (b & 3));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
