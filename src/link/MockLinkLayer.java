package link;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import util.Bytes;

/**
 * A fake implementation of a link layer that only provides communication
 * between threads, not between hosts.
 * 
 * Both readByte and sendByte potentially block, with readByte blocking being
 * much more likely.
 */
public class MockLinkLayer extends LinkLayer {
	private static final int queue_sz = 1024;

	private MockLinkLayer that;
	private BlockingQueue<Byte> bq;
	private String prefix = ">";
	private boolean debug = false;

	/** Connects this MockLinkLayer to a peer. */
	public MockLinkLayer connect(MockLinkLayer that) {
		this.that = that;
		this.bq = new ArrayBlockingQueue<Byte>(queue_sz);
		return this;
	}

	/** Sets the prefix this MockLinkLayer uses while debug logging. */
	public MockLinkLayer setPrefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	/** Enable or disable debug logging. */
	public MockLinkLayer setDebug(boolean debug) {
		this.debug = debug;
		return this;
	}

	@Override
	public void sendByte(byte data) {
		if (that != null)
			that.push(data);

		if (debug) {
			String fmt = "[MLL] sent %s '%s' %s %s\n";
			String chr = "" + ((char) data);
			String val = Byte.toString(data);

			System.out.printf(fmt, prefix, chr, val, Bytes.format(data));
		}
	}

	@Override
	public byte readByte() {
		try {
			return bq.take();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return (byte) 0;
		}
	}

	/** Add a byte of data to be consumed by the owner of this LinkLayer. */
	public void push(byte data) {
		try {
			bq.put(data);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
