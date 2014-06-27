package link.angelmaker.bitexchanger;

import java.util.BitSet;
import java.util.concurrent.ArrayBlockingQueue;

import link.angelmaker.AngelMaker;
import link.angelmaker.IncompatibleModulesException;
import link.angelmaker.manager.AMManager;
import phys.PhysicalLayer;
import util.BitSet2;

/**
 * An interface for a BitExchanger, a class that basically streams bits between
 * two sides. Error correction/detection is very limited, dependent on the
 * implementation. An implementation of this should NEVER get stuck
 * (freeze/hang).
 * 
 * This should run in an separate thread. No method of this class should be
 * blocking.
 * 
 * This should always act like it is duplex. Meaning that you should be able to
 * call read while still sending bits. It is then up to the implementation to
 * decide whether to be duplex or not. It can even choose to simulate duplex
 * (half rate, take turns). But may also just return null when it is still
 * sending.
 * 
 * @author I3anaan
 * 
 */
public interface BitExchanger {

	
	
	public void givePhysicalLayer(PhysicalLayer down);
	public void giveAMManager(AMManager manager);
	/**
	 * Enables the BitExchanger.
	 * Before enabling, calling the different methods have absolutely no guarantees.
	 * They might crash, they might work, they might do nothing.
	 */
	public void enable();
	
	/**
	 * Adds the given bits to the send queue. This means there is no requirement
	 * on |bits|
	 * 
	 * @param bits
	 */
	public void sendBits(BitSet2 bits);

	/**
	 * Empties the queue, meaning that any bits currently queued will be
	 * dropped, not sent. There is no way to tell how many bits that are not in
	 * the queue anymore reached the other end.
	 */
	public void emptyQueue();

	/**
	 * @return bits read. Will not block on read, meaning that it can return an
	 *         empty BitSet2.
	 * @ensure |result|>=0
	 */
	public BitSet2 readBits();

	/**
	 * An extension to the BitExchanger, an exchanger that has roles specified.
	 * Generally Master should be the one initiating things (sending) While
	 * Slave follows.
	 * 
	 * @author I3anaan
	 * 
	 */
	public interface MasterSlave extends BitExchanger {

		public boolean isMaster();

		public boolean isSlave();
	}

	/**
	 * A NeverEmpty BitExchanger requires there to ALWAYS be data to send. This
	 * pretty much means filler data is needed, thus it requires
	 * FillerCompatible Nodes and a manager able to serve those FillerNodes.
	 * 
	 * @author I3anaan
	 * @Requires AMManager.Server
	 * 
	 */
	public interface AlwaysSending extends BitExchanger {

	}

	public ArrayBlockingQueue<Boolean> getQueueOut();
}
