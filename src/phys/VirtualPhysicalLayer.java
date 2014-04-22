package phys;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * An implementation of a physical layer that can be used to connect different
 * threads on the same machine.
 */
public class VirtualPhysicalLayer implements PhysicalLayer {
	public Lock lock;

	private VirtualPhysicalLayer that;
	private byte state;

	/** Connect this instance to another instance of VirtualPhysicalLayer. */
	public void connect(VirtualPhysicalLayer that) {
		this.that = that;
		this.lock = new ReentrantLock();
		this.state = 0;
	}

	@Override
	public void sendByte(byte data) {
		that.takeByte(data);
	}

	@Override
	public byte readByte() {
		lock.lock();
		byte s = state;
		lock.unlock();
		return s;
	}

	/** Takes a byte sent by (possibly) another thread. */
	public void takeByte(byte data) {
		lock.lock();
		state = data;
		lock.unlock();
	}
}
