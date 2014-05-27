package link.angelmaker.bitexchanger;

import util.BitSet2;

/**
 * An interface for a BitExchanger, a class that basically streams bits between two sides.
 * Error correction/detection is very limited, dependent on the implementation.
 * An implementation of this should NEVER get stuck (freeze/hang).
 * 
 * This should run in an separate thread. No method of this class should be blocking.
 * @author I3anaan
 *
 */
public interface BitExchanger {

	/**
	 * Adds the given bits to the send queue.
	 * This means there is no requirement on |bits|
	 * @param bits
	 */
	public void sentBits(BitSet2 bits);
	
	/**
	 * Empties the queue, meaning that any bits currently queued will be dropped, not sent.
	 * There is no way to tell how many bits that are not in the queue anymore reached the other end.
	 */
	public void emptyQueue();
	
	public void queueCount();
	
	/**
	 * @return bits read.
	 * Will not block on read, meaning that it can return an empty BitSet2.
	 * @ensure |result|>=0
	 */
	public BitSet2 readBits();
}
