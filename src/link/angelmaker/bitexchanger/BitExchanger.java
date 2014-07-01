package link.angelmaker.bitexchanger;

import link.angelmaker.manager.AMManager;
import phys.PhysicalLayer;
import util.BitSet2;

/**
 * An interface for a BitExchanger, a class that basically streams bits between
 * two sides. Error correction/detection is very limited, dependent on the
 * implementation. An implementation of this should NEVER get stuck
 * (freeze/hang).
 * 
 * This should run in an separate thread.
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
}
