package link.diag;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import link.FrameLinkLayer;

/**
 * A fake implementation of a link layer that only provides communication
 * between threads, not between hosts.
 * 
 * Both readFrame and sendFrame potentially block, with readFrame blocking being
 * much more likely.
 */
public class MockLinkLayer extends FrameLinkLayer {
	private static final int queue_sz = 1024;
	private MockLinkLayer that;
	private BlockingQueue<byte[]> bq;

	/** Connects this MockLinkLayer to a peer. */
	public MockLinkLayer connect(MockLinkLayer that) {
		this.that = that;
		this.bq = new ArrayBlockingQueue<byte[]>(queue_sz);
		return this;
	}

	@Override
	public void sendFrame(byte[] data) {
		if (that != null) that.push(data);
	}

	@Override
	public byte[] readFrame() {
		try {
			return bq.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/** Add a byte array of data to be consumed by the owner of this Layer. */
	public void push(byte[] data) {
		try {
			bq.put(data);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
